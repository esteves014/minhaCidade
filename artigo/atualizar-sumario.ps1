# Abre o .docx no Word, atualiza o sumário e os campos, salva e (opcionalmente) exporta PDF.
# Uso: powershell -File atualizar-sumario.ps1 artigo.docx [artigo.pdf]
param(
  [Parameter(Mandatory = $true)][string]$Docx,
  [string]$Pdf
)
$caminho = (Resolve-Path $Docx).Path
$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0
try {
  $doc = $word.Documents.Open($caminho)
  $doc.Fields.Update() | Out-Null
  foreach ($toc in $doc.TablesOfContents) { $toc.Update() }
  $doc.Repaginate()
  $paginas = $doc.ComputeStatistics(2)
  $doc.Save()
  if ($Pdf) {
    $destino = if ([IO.Path]::IsPathRooted($Pdf)) { $Pdf } else { Join-Path (Get-Location).Path $Pdf }
    $doc.ExportAsFixedFormat($destino, 17)
  }
  $doc.Close($false)
  Write-Output "ok: $paginas paginas (incluindo capa, folha de rosto, folha de aprovacao e sumario)"
} finally {
  $word.Quit()
}
