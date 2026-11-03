# minhaCidade

App Android (Java, SQLite local) de vagas, notícias e denúncias por cidade contratante.

## Artigo científico em paralelo (ODS)

O projeto tem um artigo científico vivo em `artigo/artigo.md`, com o histórico em `artigo/diario.md`.
Sempre que alterar o app (funcionalidade, modelo de dados, segurança, textos de UI relevantes):

1. Acrescente uma linha em `artigo/diario.md` (data, versão, mudança, seções afetadas, ODS).
2. Atualize no `artigo/artigo.md` as seções afetadas (requisitos 4.1, modelo 4.3, matriz ODS 5.1, limitações 7, etc.) e o cabeçalho (versão e data).
3. Não invente dados, estatísticas, resultados de testes ou referências. Marque lacunas com `[A COMPLETAR]`, `[VERIFICAR]` ou `[A COLETAR]`.
4. Só reivindique contribuição a um ODS quando houver mecanismo no código e indicador possível; classifique como direta, indireta ou potencial (Seção 3.2) e registre o que não é reivindicado (5.4).
5. Os dados do seed são fictícios; não trate como resultado empírico.
6. Outras sessões podem mexer no código em paralelo: antes de editar o artigo, releia `artigo/diario.md` (há linhas escritas por elas) e confira no código o que foi feito.
7. Depois de atualizar o `.md`, regenere o Word, que segue o template de TCC da Fatec Lins (`artigo/template-artigo.docx`: capa, folha de rosto, folha de aprovação, sumário, Arial 12): em `artigo/`, `npm install --no-save jszip@3` (uma vez), `node gerar-docx.js artigo.md artigo.docx` e, no Windows com Word, `powershell -File atualizar-sumario.ps1 artigo.docx` (atualiza o sumário e informa o nº de páginas; o modelo pede 15 a 20). Convenções do `.md` para o gerador: linhas `**Autor:**`, `**Orientador:**`, `**Curso:**` e `**Semestre/ano:**` no início; título `Marca: subtítulo`; palavras-chave separadas por ponto; citações no texto como (AUTOR, ano); legenda de tabela ou bloco de código na linha anterior: `Quadro: Título | Fonte: texto`; `## Referências` em ordem alfabética.
