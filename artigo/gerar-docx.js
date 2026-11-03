// Converte artigo/artigo.md em .docx com formatação aproximada do modelo SBC.
// Uso: node build.js <entrada.md> <saida.docx>
const fs = require("fs");
const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell, WidthType, AlignmentType,
  BorderStyle, ShadingType, LevelFormat, HeadingLevel,
} = require("docx");

const [, , entrada, saida] = process.argv;
const FONTE = "Times New Roman";
const MONO = "Courier New";
const LARGURA = 11906 - 2 * 1701; // A4 menos margens laterais de 3 cm (DXA)

// ---------- Inline ----------
const MARCADOR = /(\[(?:A COMPLETAR|VERIFICAR|A COLETAR)[^\]]*\])/;

function runsTexto(texto, base = {}) {
  // Realça marcadores de pendência mesmo fora de crases.
  return texto.split(MARCADOR).filter((p) => p !== "").map((parte) =>
    MARCADOR.test(parte)
      ? new TextRun({ ...base, text: parte, bold: true, highlight: "yellow" })
      : new TextRun({ ...base, text: parte }));
}

function inline(texto, base = {}) {
  const runs = [];
  for (const parte of texto.split(/(\*\*[^*]+\*\*|\*[^*\s][^*]*\*|`[^`]+`)/)) {
    if (!parte) continue;
    if (parte.startsWith("**") && parte.endsWith("**") && parte.length > 4) {
      runs.push(...runsTexto(parte.slice(2, -2), { ...base, bold: true }));
    } else if (parte.startsWith("`") && parte.endsWith("`")) {
      const t = parte.slice(1, -1);
      runs.push(MARCADOR.test(t)
        ? new TextRun({ ...base, text: t, bold: true, highlight: "yellow" })
        : new TextRun({ ...base, text: t, font: MONO, size: Math.round((base.size || 24) * 0.85) }));
    } else if (parte.startsWith("*") && parte.endsWith("*") && parte.length > 2) {
      runs.push(...runsTexto(parte.slice(1, -1), { ...base, italics: true }));
    } else {
      runs.push(...runsTexto(parte, base));
    }
  }
  return runs;
}

// ---------- Parser de Markdown (subconjunto usado no artigo) ----------
function parse(md) {
  let linhas = md.replace(/\r\n/g, "\n").split("\n");
  if (linhas[0].trim() === "---") {
    const fim = linhas.indexOf("---", 1);
    linhas = linhas.slice(fim + 1);
  }
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
    const h = l.match(/^(#{1,3}) (.*)$/);
    if (h) { flushTudo(); blocos.push({ t: "h", nivel: h[1].length, texto: h[2] }); continue; }
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

// ---------- Construção do documento ----------
const numeracoes = [];
function novaNumeracao() {
  const ref = "num-" + (numeracoes.length + 1);
  numeracoes.push({
    reference: ref,
    levels: [{ level: 0, format: LevelFormat.DECIMAL, text: "%1.", alignment: AlignmentType.LEFT,
      style: { paragraph: { indent: { left: 720, hanging: 360 } } } }],
  });
  return ref;
}

function tabela(linhas) {
  const celulas = linhas
    .filter((l) => !/^\|\s*-/.test(l))
    .map((l) => l.trim().replace(/^\||\|$/g, "").split(/(?<!\\)\|/).map((c) => c.trim()));
  const n = Math.max(...celulas.map((r) => r.length));
  // Largura proporcional ao texto (com mínimo), somando exatamente LARGURA.
  const peso = Array.from({ length: n }, (_, c) =>
    Math.min(60, Math.max(8, Math.max(...celulas.map((r) => (r[c] || "").length)))));
  const soma = peso.reduce((a, b) => a + b, 0);
  const larg = peso.map((p) => Math.floor((p / soma) * LARGURA));
  larg[n - 1] += LARGURA - larg.reduce((a, b) => a + b, 0);
  const borda = { style: BorderStyle.SINGLE, size: 4, color: "808080" };
  const bordas = { top: borda, bottom: borda, left: borda, right: borda };
  const rows = celulas.map((r, ri) => new TableRow({
    tableHeader: ri === 0,
    children: Array.from({ length: n }, (_, c) => new TableCell({
      width: { size: larg[c], type: WidthType.DXA },
      borders: bordas,
      margins: { top: 40, bottom: 40, left: 80, right: 80 },
      shading: ri === 0 ? { type: ShadingType.CLEAR, fill: "D9D9D9", color: "auto" } : undefined,
      children: [new Paragraph({
        alignment: AlignmentType.LEFT,
        children: inline(r[c] || "", { size: 18, bold: ri === 0 ? true : undefined }),
      })],
    })),
  }));
  return new Table({ width: { size: LARGURA, type: WidthType.DXA }, columnWidths: larg, rows });
}

function montar(blocos) {
  const filhos = [];
  let secao = "";
  let primeiroH1 = true;
  for (const b of blocos) {
    if (b.t === "h") {
      if (b.nivel === 1) {
        filhos.push(new Paragraph({ heading: HeadingLevel.TITLE, alignment: AlignmentType.CENTER,
          children: inline(b.texto, { bold: true, size: 32 }) }));
      } else {
        secao = b.texto;
        primeiroH1 = false;
        filhos.push(new Paragraph({
          heading: b.nivel === 2 ? HeadingLevel.HEADING_1 : HeadingLevel.HEADING_2,
          children: inline(b.texto, { bold: true, size: b.nivel === 2 ? 26 : 24 }),
        }));
      }
    } else if (b.t === "p") {
      if (primeiroH1 && /^\*\*Autor/.test(b.texto)) {
        filhos.push(new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 120, after: 240 },
          children: inline(b.texto.replace(/^\*\*Autor:\*\*\s*/, "")) }));
      } else {
        const ref = /^Refer/.test(secao);
        const semRecuo = ref || /^\*\*(Palavras-chave|Keywords|Objetivo|Contribui|Organiza)/.test(b.texto);
        filhos.push(new Paragraph({
          alignment: ref ? AlignmentType.LEFT : AlignmentType.JUSTIFIED,
          indent: semRecuo ? undefined : { firstLine: 720 },
          spacing: { after: ref ? 160 : 120 },
          children: inline(b.texto),
        }));
      }
    } else if (b.t === "quote") {
      filhos.push(new Paragraph({
        alignment: AlignmentType.JUSTIFIED, spacing: { before: 120, after: 200 },
        shading: { type: ShadingType.CLEAR, fill: "F2F2F2", color: "auto" },
        border: { left: { style: BorderStyle.SINGLE, size: 12, color: "808080", space: 6 } },
        children: inline(b.texto, { italics: true, size: 20 }),
      }));
    } else if (b.t === "list") {
      const ref = b.ordenada ? novaNumeracao() : null;
      for (const item of b.itens) {
        filhos.push(new Paragraph({
          alignment: AlignmentType.JUSTIFIED, spacing: { after: 60 },
          ...(b.ordenada ? { numbering: { reference: ref, level: 0 } } : { numbering: { reference: "bullets", level: 0 } }),
          children: inline(item),
        }));
      }
      filhos.push(new Paragraph({ spacing: { after: 60 }, children: [] }));
    } else if (b.t === "table") {
      filhos.push(tabela(b.linhas));
      filhos.push(new Paragraph({ spacing: { after: 120 }, children: [] }));
    } else if (b.t === "code") {
      b.linhas.forEach((linha) => filhos.push(new Paragraph({
        spacing: { after: 0 },
        shading: { type: ShadingType.CLEAR, fill: "F2F2F2", color: "auto" },
        children: [new TextRun({ text: linha === "" ? " " : linha, font: MONO, size: 17 })],
      })));
      filhos.push(new Paragraph({ spacing: { after: 120 }, children: [] }));
    }
  }
  return filhos;
}

const filhos = montar(parse(fs.readFileSync(entrada, "utf8")));
const doc = new Document({
  creator: "minhaCidade",
  title: "minhaCidade e as ODS",
  styles: {
    default: { document: { run: { font: FONTE, size: 24 } } },
    paragraphStyles: [
      { id: "Title", name: "Title", basedOn: "Normal", next: "Normal",
        run: { font: FONTE, size: 32, bold: true, color: "000000" },
        paragraph: { alignment: AlignmentType.CENTER, spacing: { before: 0, after: 240 } } },
      { id: "Heading1", name: "heading 1", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { font: FONTE, size: 26, bold: true, color: "000000" },
        paragraph: { spacing: { before: 360, after: 120 }, outlineLevel: 0, keepNext: true } },
      { id: "Heading2", name: "heading 2", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { font: FONTE, size: 24, bold: true, color: "000000" },
        paragraph: { spacing: { before: 240, after: 120 }, outlineLevel: 1, keepNext: true } },
    ],
  },
  numbering: {
    config: [
      { reference: "bullets", levels: [{ level: 0, format: LevelFormat.BULLET, text: "•", alignment: AlignmentType.LEFT,
        style: { paragraph: { indent: { left: 720, hanging: 360 } } } }] },
      ...numeracoes,
    ],
  },
  sections: [{
    properties: {
      page: {
        size: { width: 11906, height: 16838 }, // A4
        margin: { top: 1985, bottom: 1417, left: 1701, right: 1701 }, // 3,5 / 2,5 / 3 / 3 cm
      },
    },
    children: filhos,
  }],
});

Packer.toBuffer(doc).then((buf) => {
  fs.writeFileSync(saida, buf);
  console.log("ok", saida, buf.length, "bytes");
});
