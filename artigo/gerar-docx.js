// Converte artigo/artigo.md em .docx usando o template de TCC da Fatec Lins (template-artigo.docx)
// como base: capa, folha de rosto, folha de aprovação, sumário e corpo do trabalho.
// Uso: node gerar-docx.js <entrada.md> <saida.docx> [template.docx]
// Requer: npm install --no-save jszip@3
const fs = require("fs");
const path = require("path");
const JSZip = require("jszip");

const [, , entrada, saida, modeloArg] = process.argv;
if (!entrada || !saida) {
  console.error("Uso: node gerar-docx.js <entrada.md> <saida.docx> [template.docx]");
  process.exit(1);
}
const MODELO = modeloArg || path.join(__dirname, "template-artigo.docx");
const FONTE = "Arial";
const MONO = "Courier New";
const LARGURA = 11907 - 1701 - 1134; // A4 menos margens do template (esq. 3 cm, dir. 2 cm), em DXA

// ---------- XML ----------
const esc = (s) => s.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
const desesc = (s) => s.replace(/&lt;/g, "<").replace(/&gt;/g, ">").replace(/&quot;/g, '"').replace(/&amp;/g, "&");

// Ordem dos filhos de rPr/pPr segue o esquema do Word.
function run(t, o = {}) {
  const f = o.font || FONTE;
  let r = `<w:rFonts w:ascii="${f}" w:hAnsi="${f}" w:cs="${f}"/>`;
  if (o.b) r += "<w:b/><w:bCs/>";
  if (o.i) r += "<w:i/><w:iCs/>";
  if (o.color) r += `<w:color w:val="${o.color}"/>`;
  if (o.sz) r += `<w:sz w:val="${o.sz}"/><w:szCs w:val="${o.sz}"/>`;
  if (o.hl) r += '<w:highlight w:val="yellow"/>';
  if (o.sup) r += '<w:vertAlign w:val="superscript"/>';
  return `<w:r><w:rPr>${r}</w:rPr><w:t xml:space="preserve">${esc(t)}</w:t></w:r>`;
}

function para(runs, p = {}) {
  let ppr = "";
  if (p.style) ppr += `<w:pStyle w:val="${p.style}"/>`;
  if (p.keepNext) ppr += "<w:keepNext/>";
  if (p.keepLines) ppr += "<w:keepLines/>";
  if (p.semNumero) ppr += '<w:numPr><w:ilvl w:val="0"/><w:numId w:val="0"/></w:numPr>';
  if (p.shd) ppr += `<w:shd w:val="clear" w:color="auto" w:fill="${p.shd}"/>`;
  if (p.spacing) ppr += `<w:spacing ${p.spacing}/>`;
  if (p.ind) ppr += `<w:ind ${p.ind}/>`;
  if (p.jc) ppr += `<w:jc w:val="${p.jc}"/>`;
  return `<w:p>${ppr ? `<w:pPr>${ppr}</w:pPr>` : ""}${runs}</w:p>`;
}

const textoDe = (xml) => desesc((xml.match(/<w:t(?:\s[^>]*)?>[^<]*<\/w:t>/g) || [])
  .map((t) => t.replace(/<[^>]+>/g, "")).join(""));

// ---------- Inline (negrito, itálico, código, marcadores de pendência) ----------
const MARCADOR = /(\[(?:A COMPLETAR|VERIFICAR|A COLETAR)[^\]]*\])/;

function runsTexto(texto, base) {
  return texto.split(MARCADOR).filter((p) => p !== "").map((parte) =>
    MARCADOR.test(parte) ? run(parte, { ...base, b: true, hl: true }) : run(parte, base)).join("");
}

function inline(texto, base = {}) {
  let out = "";
  for (const parte of texto.split(/(\*\*[^*]+\*\*|\*[^*\s][^*]*\*|`[^`]+`)/)) {
    if (!parte) continue;
    if (parte.startsWith("**") && parte.endsWith("**") && parte.length > 4) {
      out += runsTexto(parte.slice(2, -2), { ...base, b: true });
    } else if (parte.startsWith("`") && parte.endsWith("`")) {
      const t = parte.slice(1, -1);
      out += MARCADOR.test(t)
        ? run(t, { ...base, b: true, hl: true })
        : run(t, { ...base, font: MONO, sz: Math.round((base.sz || 24) * 0.85) });
    } else if (parte.startsWith("*") && parte.endsWith("*") && parte.length > 2) {
      out += runsTexto(parte.slice(1, -1), { ...base, i: true });
    } else {
      out += runsTexto(parte, base);
    }
  }
  return out;
}

// ---------- Parser de Markdown (subconjunto usado no artigo) ----------
function parse(md) {
  let linhas = md.replace(/\r\n/g, "\n").split("\n");
  if (linhas[0].trim() === "---") linhas = linhas.slice(linhas.indexOf("---", 1) + 1);
  const blocos = [];
  let par = [], cit = [], tab = [], lista = null;
  const flushPar = () => { if (par.length) blocos.push({ t: "p", texto: par.join(" ") }); par = []; };
  const flushCit = () => { if (cit.length) blocos.push({ t: "quote", texto: cit.join(" ") }); cit = []; };
  const flushTab = () => { if (tab.length) blocos.push({ t: "table", linhas: tab }); tab = []; };
  const flushLista = () => { if (lista) blocos.push(lista); lista = null; };
  const flushTudo = () => { flushPar(); flushCit(); flushTab(); flushLista(); };

  for (let i = 0; i < linhas.length; i++) {
    const l = linhas[i];
    if (l.startsWith("```")) {
      flushTudo();
      const codigo = [];
      for (i++; i < linhas.length && !linhas[i].startsWith("```"); i++) codigo.push(linhas[i]);
      blocos.push({ t: "code", linhas: codigo });
      continue;
    }
    if (l.trim() === "") { flushTudo(); continue; }
    if (l.trim() === "---") { flushTudo(); continue; }
    const h = l.match(/^(#{1,4}) (.*)$/);
    if (h) { flushTudo(); blocos.push({ t: "h", nivel: h[1].length, texto: h[2] }); continue; }
    const leg = l.match(/^Quadro: (.*)$/);
    if (leg) { flushTudo(); blocos.push({ t: "legenda", texto: leg[1] }); continue; }
    if (l.startsWith(">")) { flushPar(); flushTab(); flushLista(); cit.push(l.replace(/^>\s?/, "")); continue; }
    if (l.startsWith("|")) { flushPar(); flushCit(); flushLista(); tab.push(l); continue; }
    const num = l.match(/^(\d+)\. (.*)$/), bul = l.match(/^- (.*)$/);
    if (num || bul) {
      flushPar(); flushCit(); flushTab();
      const ordenada = !!num;
      if (!lista || lista.ordenada !== ordenada) { flushLista(); lista = { t: "list", ordenada, itens: [] }; }
      lista.itens.push(num ? num[2] : bul[1]);
      continue;
    }
    if (lista && /^\s+\S/.test(l)) { lista.itens[lista.itens.length - 1] += " " + l.trim(); continue; }
    flushLista(); flushCit(); flushTab();
    par.push(l.trim());
  }
  flushTudo();
  return blocos;
}

// ---------- Blocos do corpo ----------
function tabela(linhas) {
  const celulas = linhas
    .filter((l) => !/^\|\s*-/.test(l))
    .map((l) => l.trim().replace(/^\||\|$/g, "").split(/(?<!\\)\|/).map((c) => c.trim()));
  const n = Math.max(...celulas.map((r) => r.length));
  // Largura proporcional ao texto (com mínimo e máximo), somando exatamente LARGURA.
  const peso = Array.from({ length: n }, (_, c) =>
    Math.min(60, Math.max(8, Math.max(...celulas.map((r) => (r[c] || "").length)))));
  const soma = peso.reduce((a, b) => a + b, 0);
  const larg = peso.map((p) => Math.floor((p / soma) * LARGURA));
  larg[n - 1] += LARGURA - larg.reduce((a, b) => a + b, 0);
  const borda = (lado) => `<w:${lado} w:val="single" w:sz="4" w:space="0" w:color="808080"/>`;
  const bordas = ["top", "left", "bottom", "right", "insideH", "insideV"].map(borda).join("");
  const linhasXml = celulas.map((r, ri) => {
    const cels = Array.from({ length: n }, (_, c) => {
      const shd = ri === 0 ? '<w:shd w:val="clear" w:color="auto" w:fill="D9D9D9"/>' : "";
      const p = para(inline(r[c] || "", { sz: 18, b: ri === 0 || undefined }),
        { jc: "left", spacing: 'w:before="0" w:after="0"' });
      return `<w:tc><w:tcPr><w:tcW w:w="${larg[c]}" w:type="dxa"/>${shd}</w:tcPr>${p}</w:tc>`;
    }).join("");
    return `<w:tr><w:trPr><w:cantSplit/>${ri === 0 ? "<w:tblHeader/>" : ""}</w:trPr>${cels}</w:tr>`;
  }).join("");
  return `<w:tbl><w:tblPr><w:tblW w:w="${LARGURA}" w:type="dxa"/><w:tblBorders>${bordas}</w:tblBorders>` +
    `<w:tblLayout w:type="fixed"/><w:tblCellMar><w:top w:w="40" w:type="dxa"/><w:left w:w="80" w:type="dxa"/>` +
    `<w:bottom w:w="40" w:type="dxa"/><w:right w:w="80" w:type="dxa"/></w:tblCellMar></w:tblPr>` +
    `<w:tblGrid>${larg.map((w) => `<w:gridCol w:w="${w}"/>`).join("")}</w:tblGrid>${linhasXml}</w:tbl>`;
}

function codigo(linhasCodigo) {
  return linhasCodigo.map((linha) => para(run(linha === "" ? " " : linha, { font: MONO, sz: 17 }),
    { shd: "F2F2F2", keepLines: true, spacing: 'w:before="0" w:after="0"' })).join("");
}

// ---------- Metadados (linhas **Autor:**, **Orientador:**, ...) ----------
function extrairMeta(blocos) {
  const meta = { autor: "", orientador: "", curso: "", semestre: "" };
  const chaves = { "Autor": "autor", "Orientador": "orientador", "Curso": "curso", "Semestre/ano": "semestre" };
  const resto = blocos.filter((b) => {
    const m = b.t === "p" && b.texto.match(/^\*\*(Autor|Orientador|Curso|Semestre\/ano):\*\*\s*(.*)$/);
    if (m) meta[chaves[m[1]]] = m[2].trim();
    return !m;
  });
  const pendente = (rotulo) => `[A COMPLETAR: ${rotulo}]`;
  meta.autor = meta.autor || pendente("nome do(s) aluno(s)");
  meta.orientador = meta.orientador || pendente("titulação e nome do orientador");
  meta.curso = meta.curso || pendente("nome do curso");
  meta.semestre = meta.semestre || pendente("Xº semestre/ano");
  return { meta, resto };
}

// ---------- Corpo do trabalho (a partir da página do título, no template) ----------
function montarCorpo(blocos, meta) {
  const h1 = blocos.find((b) => b.t === "h" && b.nivel === 1);
  const titulo = h1 ? h1.texto : "";
  const autores = meta.autor.split(/\s*;\s*/).filter(Boolean);
  const nA = autores.length;
  const supA = nA > 1 ? `1,${nA}` : "1";
  const out = [];

  // Bloco de título e autoria (modelo: título, autores, orientador, afiliações).
  out.push(para(run(titulo.toUpperCase(), { b: true, sz: 28 }), { jc: "center", spacing: 'w:before="0" w:after="240"' }));
  out.push(para(autores.map((a, i) => inline(a) + run(String(i + 1), { sup: true })).join(run(", ")),
    { jc: "center", spacing: 'w:after="0"' }));
  out.push(para(inline(meta.orientador) + run(String(nA + 1), { sup: true }),
    { jc: "center", spacing: 'w:after="240"' }));
  const afil = `da Faculdade de Tecnologia de Lins Prof. Antônio Seabra – Fatec, Lins – SP, Brasil`;
  out.push(para(run(supA, { sup: true }) + run(nA > 1 ? "Acadêmicos do Curso de " : "Acadêmico do Curso de ") +
    inline(meta.curso) + run(" " + afil), { jc: "center", spacing: 'w:after="0"' }));
  out.push(para(run(String(nA + 1), { sup: true }) + run("Docente do Curso de ") + inline(meta.curso) + run(" " + afil),
    { jc: "center", spacing: 'w:after="120"' }));

  let secao = "", secNum = "", legendaPendente = null, contLegenda = 0;
  const palavrasResumo = [];
  const legenda = (l, tipo) => {
    contLegenda++;
    const [tit, fonte] = l.split(/\s*\|\s*Fonte:\s*/);
    const rotulo = `Quadro ${secNum ? secNum + "." : ""}${contLegenda} – ${tit}`;
    return {
      antes: para(inline(rotulo), { keepNext: true, jc: "left", spacing: 'w:before="120" w:after="60"' }),
      depois: para(inline("Fonte: " + (fonte || "elaborado pelo autor."), { sz: 20 }),
        { jc: "left", spacing: 'w:before="60" w:after="240"' }),
    };
  };

  for (const b of blocos) {
    if (b.t === "h") {
      if (b.nivel === 1) continue; // título já emitido acima
      const m = b.texto.match(/^(\d+(?:\.\d+)*)\s+(.*)$/);
      if (b.nivel === 2) {
        secao = m ? m[2] : b.texto;
        secNum = m ? m[1] : "";
        contLegenda = 0;
        let txt = (m ? m[2] : b.texto).toUpperCase();
        if (/^REFER[ÊE]NCIAS$/.test(txt)) txt = "REFERÊNCIAS BIBLIOGRÁFICAS";
        out.push(para(run((m ? m[1] + " " : "") + txt, { b: true, sz: 28 }),
          { style: "Ttulo1", semNumero: true, jc: "left", spacing: 'w:before="360" w:after="240"' }));
      } else {
        const rotulo = m ? m[1] + " " : "";
        const txt = m ? m[2] : b.texto;
        const caixaAlta = b.nivel === 3;
        out.push(para(run(rotulo + (caixaAlta ? txt.toUpperCase() : txt), { b: true, color: "auto", sz: 24 }),
          { style: "Ttulo2", jc: "both", spacing: `w:before="${caixaAlta ? 240 : 120}" w:after="120" w:line="240" w:lineRule="auto"` }));
      }
    } else if (b.t === "p") {
      const resumo = /^(Resumo|Abstract)$/.test(secao);
      const ref = /^Refer/.test(secao);
      const chaves = b.texto.match(/^\*\*(Palavras-chave|Keywords):\*\*\s*(.*)$/);
      if (resumo && chaves) {
        // Modelo: rótulo sem negrito; palavras separadas por ponto.
        out.push(para(inline(`${chaves[1]}: ${chaves[2]}`), { jc: "both", spacing: 'w:before="0" w:after="240" w:line="360" w:lineRule="auto"' }));
      } else if (resumo) {
        palavrasResumo.push([secao, b.texto.split(/\s+/).length]);
        out.push(para(inline(b.texto), { jc: "both", spacing: 'w:before="0" w:after="240"' }));
      } else if (ref) {
        out.push(para(inline(b.texto, { sz: 20 }), { jc: "left", spacing: 'w:before="0" w:after="240"' }));
      } else {
        out.push(para(inline(b.texto), { jc: "both", ind: 'w:firstLine="709"', spacing: 'w:before="0" w:after="120"' }));
      }
    } else if (b.t === "quote") {
      // Nota recuada 4 cm, fonte 10 (como a citação longa do modelo).
      out.push(para(inline(b.texto, { sz: 20 }), { jc: "both", ind: 'w:left="2268"', spacing: 'w:before="120" w:after="240"' }));
    } else if (b.t === "list") {
      b.itens.forEach((item, i) => {
        const marca = b.ordenada ? `${i + 1}.` : "•";
        out.push(para(run(marca) + "<w:r><w:tab/></w:r>" + inline(item),
          { jc: "both", ind: 'w:left="1069" w:hanging="360"', spacing: 'w:before="0" w:after="60"' }));
      });
      out.push(para("", { spacing: 'w:before="0" w:after="0" w:line="120" w:lineRule="exact"' }));
    } else if (b.t === "legenda") {
      legendaPendente = legenda(b.texto);
    } else if (b.t === "table" || b.t === "code") {
      const leg = legendaPendente;
      legendaPendente = null;
      if (leg) out.push(leg.antes);
      out.push(b.t === "table" ? tabela(b.linhas) : codigo(b.linhas));
      out.push(leg ? leg.depois : para("", { spacing: 'w:before="0" w:after="120"' }));
    }
  }
  for (const [nome, n] of palavrasResumo) if (n > 250) console.warn(`aviso: ${nome} tem ${n} palavras (limite do modelo: 250)`);
  return out.join("");
}

// ---------- Substituições na parte pré-textual do template ----------
const PAR_RE = /<w:p(?:\s[^>]*[^/>])?>[\s\S]*?<\/w:p>/g;

// Reescreve o texto de um parágrafo simples mantendo seu pPr e a formatação do primeiro trecho.
function reescrever(p, novoTexto) {
  const ppr = (p.match(/<w:pPr>[\s\S]*?<\/w:pPr>/) || [""])[0];
  const primeiro = (p.match(/<w:r(?:\s[^>]*)?>(?:(?!<\/w:r>)[\s\S])*?<w:t[\s>][\s\S]*?<\/w:r>/) || [""])[0];
  const rpr = (primeiro.match(/<w:rPr>[\s\S]*?<\/w:rPr>/) || [""])[0];
  const destaque = (r) => {
    if (!r) return "<w:rPr><w:highlight w:val=\"yellow\"/></w:rPr>";
    const pos = r.search(/<w:vertAlign|<w:lang/);
    return pos >= 0 ? r.slice(0, pos) + '<w:highlight w:val="yellow"/>' + r.slice(pos) : r.replace("</w:rPr>", '<w:highlight w:val="yellow"/></w:rPr>');
  };
  const runs = novoTexto.split(MARCADOR).filter((x) => x !== "").map((parte) =>
    `<w:r>${MARCADOR.test(parte) ? destaque(rpr) : rpr}<w:t xml:space="preserve">${esc(parte)}</w:t></w:r>`).join("");
  return `<w:p>${ppr}${runs}</w:p>`;
}

function preencherPretextual(xml, meta, titulo, subtitulo) {
  const curso = meta.curso;
  const regras = [
    [/^Curso Superior de Tecnologia em/, () => `Curso Superior de Tecnologia em ${curso}`],
    [/^NOME\(S\) DO\(S\) ALUNO/, () => meta.autor.split(/\s*;\s*/).join("; ").toUpperCase()],
    [/^\s*T.TULO DO TRABALHO\s*$/, () => titulo.toUpperCase()],
    [/^\s*SUBT.TULO\s*$/, () => subtitulo.toUpperCase()],
    [/^\s*T.tulo do trabalho:\s*$/, () => titulo.toUpperCase() + (subtitulo ? ":" : "")],
    [/^\s*Subt.tulo \(se houver\)\s*$/, () => subtitulo.toUpperCase()],
    [/^Trabalho de Conclus.o de Curso apresentado . Faculdade de Tecnologia de Lins para/, (t) => t.replace("XXXX", curso).replace(/\s+$/, "")],
    [/^Orientador: Prof/, () => `Orientador: ${meta.orientador}`],
    [/^Trabalho de Conclus.o de Curso apresentado . Faculdade de Tecnologia de Lins como parte/,
      () => `Trabalho de Conclusão de Curso apresentado à Faculdade de Tecnologia de Lins como parte dos requisitos para obtenção do título de Tecnólogo em ${curso} sob orientação do prof. ${meta.orientador.replace(/^Prof\.?\s*/i, "")}.`],
    [/^X. SEMESTRE\/ANO/, () => meta.semestre.toUpperCase()],
  ];
  const toc = '<w:p><w:pPr><w:jc w:val="both"/></w:pPr><w:r><w:fldChar w:fldCharType="begin" w:dirty="true"/></w:r>' +
    '<w:r><w:instrText xml:space="preserve"> TOC \\o "1-3" \\h \\z \\u </w:instrText></w:r>' +
    '<w:r><w:fldChar w:fldCharType="separate"/></w:r>' +
    run("Atualize o campo (botão direito > Atualizar campo) para gerar o sumário.") +
    '<w:r><w:fldChar w:fldCharType="end"/></w:r></w:p>';

  return xml.replace(PAR_RE, (p) => {
    if (/<w:drawing|<w:pict|mc:AlternateContent/.test(p)) return p;
    const t = textoDe(p);
    if (/^Sugest.o: usar o sum.rio/.test(t)) return toc;
    for (const [re, fn] of regras) if (re.test(t)) return reescrever(p, fn(t));
    return p;
  });
}

// O modelo posiciona "LINS/SP" e as assinaturas com parágrafos vazios, pensando em títulos curtos.
// Títulos longos empurrariam o rodapé da folha para a página seguinte; por isso retiramos parágrafos
// vazios na proporção das linhas extras estimadas (cpl = caracteres por linha de cada bloco).
function ajustarFolhas(xml, meta, titulo, subtitulo) {
  const linhas = (txt, cpl) => Math.max(1, Math.ceil(txt.length / cpl));
  const autores = meta.autor.split(/\s*;\s*/).join("; ");
  const extraTitulo = (linhas(subtitulo, 50) - 1) * 1.2 + (linhas(titulo, 40) - 1) * 1.2;
  const extraCurso = linhas("Curso Superior de Tecnologia em " + meta.curso, 56) - 1;
  const extraAutor = linhas(autores, 58) - 1;
  const base = extraTitulo + extraCurso + extraAutor;
  const blocoRosto = linhas("Trabalho de Conclusão de Curso apresentado à Faculdade de Tecnologia de Lins para a obtenção do título de Tecnólogo (a) em " + meta.curso, 42) - 3;
  const orient = linhas("Orientador: " + meta.orientador, 42) - 1;
  const blocoAprov = linhas("Trabalho de Conclusão de Curso apresentado à Faculdade de Tecnologia de Lins como parte dos requisitos para obtenção do título de Tecnólogo em " + meta.curso + " sob orientação do prof. " + meta.orientador, 42) - 4;
  const remocoes = [
    { ancora: /^LINS\/SP$/, ocorrencia: 0, n: Math.min(23, Math.ceil(base) + 1) },
    { ancora: /^LINS\/SP$/, ocorrencia: 1, n: Math.min(18, Math.ceil(base + 0.85 * (blocoRosto + orient)) + 1) },
    { ancora: /^Trabalho de Conclus.o de Curso apresentado . Faculdade de Tecnologia de Lins como parte/, ocorrencia: 0,
      n: Math.min(3, Math.max(0, Math.ceil(0.4 * (extraTitulo + extraAutor + 0.85 * blocoAprov)))) },
  ];
  const pars = [...xml.matchAll(PAR_RE)].map((m) => ({ ini: m.index, fim: m.index + m[0].length, p: m[0], t: textoDe(m[0]) }));
  const vazio = (q) => q.t.trim() === "" && !/<w:drawing|<w:pict|<w:sectPr|<w:br/.test(q.p);
  const remover = new Set();
  for (const r of remocoes) {
    const k = pars.map((q, i) => (r.ancora.test(q.t.trim()) ? i : -1)).filter((i) => i >= 0)[r.ocorrencia];
    if (k === undefined) continue;
    for (let j = k - 1, n = r.n; j >= 0 && n > 0 && vazio(pars[j]); j--, n--) remover.add(j);
  }
  // A folha de aprovação (3.º "NOME(S)...", já substituído pelo autor) começa em página nova.
  const autorMaiusc = autores.toUpperCase();
  const idxAutor = pars.map((q, i) => (q.t.trim() === autorMaiusc ? i : -1)).filter((i) => i >= 0);
  const quebra = idxAutor[idxAutor.length - 1];
  let out = "", pos = 0;
  pars.forEach((q, i) => {
    out += xml.slice(pos, q.ini);
    pos = q.fim;
    if (remover.has(i)) return;
    out += i === quebra
      ? q.p.replace(/<w:pPr>(<w:pStyle[^>]*\/>)?/, (m, st) => `<w:pPr>${st || ""}<w:pageBreakBefore/>`)
      : q.p;
  });
  return out + xml.slice(pos);
}

// ---------- Montagem ----------
async function main() {
  const { meta, resto } = extrairMeta(parse(fs.readFileSync(entrada, "utf8")));
  const h1 = resto.find((b) => b.t === "h" && b.nivel === 1);
  const completo = h1 ? h1.texto : "";
  const dp = completo.indexOf(":");
  const titulo = dp > 0 ? completo.slice(0, dp).trim() : completo;
  const subtitulo = dp > 0 ? completo.slice(dp + 1).trim() : "";

  const zip = await JSZip.loadAsync(fs.readFileSync(MODELO));
  let xml = await zip.file("word/document.xml").async("string");

  // 1. Remove as caixas de instrução em vermelho do modelo, "(Capa.)", "(Folha de rosto.)", etc.
  xml = xml.replace(/<mc:AlternateContent>[\s\S]*?<\/mc:AlternateContent>/g,
    (alt) => (textoDe(alt).trim().startsWith("(") ? "" : alt));

  // 2. O corpo do modelo começa no último "TÍTULO DO TRABALHO" (página do artigo) e vai até o sectPr final.
  const marcas = [...xml.matchAll(/<w:t(?:\s[^>]*)?>T.TULO DO TRABALHO<\/w:t>/g)];
  if (!marcas.length) throw new Error("Template inesperado: título do corpo não encontrado");
  const idxTit = marcas[marcas.length - 1].index;
  const inicio = Math.max(xml.lastIndexOf("<w:p>", idxTit), xml.lastIndexOf("<w:p ", idxTit));
  const fim = xml.lastIndexOf("<w:sectPr");
  const pretextual = ajustarFolhas(preencherPretextual(xml.slice(0, inicio), meta, titulo, subtitulo), meta, titulo, subtitulo);
  xml = pretextual + montarCorpo(resto, meta) + xml.slice(fim);
  zip.file("word/document.xml", xml);

  // 3. Propriedades do documento.
  const nomeAutor = /\[A COMPLETAR/.test(meta.autor) ? "minhaCidade" : meta.autor;
  let core = await zip.file("docProps/core.xml").async("string");
  core = core.replace(/<dc:title>[\s\S]*?<\/dc:title>/, `<dc:title>${esc(completo)}</dc:title>`)
    .replace(/<dc:creator>[\s\S]*?<\/dc:creator>/, `<dc:creator>${esc(nomeAutor)}</dc:creator>`)
    .replace(/<cp:lastModifiedBy>[\s\S]*?<\/cp:lastModifiedBy>/, `<cp:lastModifiedBy>${esc(nomeAutor)}</cp:lastModifiedBy>`);
  zip.file("docProps/core.xml", core);

  const buf = await zip.generateAsync({ type: "nodebuffer", compression: "DEFLATE" });
  fs.writeFileSync(saida, buf);
  console.log("ok", saida, buf.length, "bytes");
}

main().catch((e) => { console.error(e); process.exit(1); });
