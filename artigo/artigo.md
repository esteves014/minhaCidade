---
versao_artigo: 0.6
data: 2026-11-13
versao_app: 1.0 (versionCode 1; esquema do banco v4)
status: rascunho em evolução — atualizado junto com o código
---

# minhaCidade: um aplicativo móvel municipal para vagas de emprego, notícias locais e denúncias urbanas, e sua contribuição aos Objetivos de Desenvolvimento Sustentável

**Autor:** [A COMPLETAR: nome completo do aluno]

**Orientador:** [A COMPLETAR: titulação e nome do orientador]

**Curso:** [A COMPLETAR: nome do curso superior de tecnologia]

**Semestre/ano:** [A COMPLETAR: Xº semestre/ano da entrega do TCC]

> **Convenções deste documento.** `[A COMPLETAR]` marca informação que só o autor pode fornecer.
> `[VERIFICAR]` marca afirmação externa que precisa ser conferida na fonte antes da submissão.
> `[A COLETAR]` marca dado empírico que ainda não existe. Nenhum resultado empírico é afirmado
> sem que tenha sido coletado; o que está descrito como "implementado" corresponde ao código-fonte
> da versão indicada no cabeçalho.

## Resumo

Municípios de pequeno porte costumam ter canais dispersos para três necessidades cotidianas do
cidadão: encontrar emprego, acompanhar o noticiário local e comunicar problemas de infraestrutura
urbana à prefeitura. Este trabalho apresenta o *minhaCidade*, um aplicativo Android que reúne esses
três serviços em um único ponto de acesso, segmentado por município contratante, com cadastro
por CPF, protocolo de denúncias com posição geográfica opcional (GPS ou ponto escolhido em mapa),
pareceres da prefeitura e notificações ao cidadão. O artefato é
desenvolvido segundo a abordagem *Design Science Research* e analisado à luz da Agenda 2030 da ONU:
cada funcionalidade é relacionada a metas específicas dos Objetivos de Desenvolvimento Sustentável
(ODS 8, 9, 11 e 16, com contribuições indiretas aos ODS 3, 6, 10 e 17), classificando a contribuição como direta,
indireta ou potencial e propondo indicadores calculáveis a partir do próprio modelo de dados. Na
versão atual (protótipo com dados fictícios para três cidades paulistas) não há avaliação com usuários;
o trabalho registra o alinhamento projetado, as limitações e o plano de validação.

**Palavras-chave:** Governo digital. Participação cidadã. Objetivos de Desenvolvimento Sustentável.
Aplicativo móvel. Municípios de pequeno porte. Android.

## Abstract

Small municipalities often lack an integrated channel for three everyday citizen needs: finding
jobs, following local news, and reporting urban infrastructure problems to the city hall. This paper
presents *minhaCidade*, an Android application that brings these services together, scoped by
contracted municipality, with CPF-based registration, report protocols with optional geographic
position (GPS or a point chosen on a map), official city responses and push notifications. The artifact is developed under a Design Science Research approach and analysed
against the UN 2030 Agenda: each feature is mapped to specific SDG targets (SDGs 8, 9, 11 and 16,
with indirect links to SDGs 3, 6, 10 and 17), classified as direct, indirect or potential
contribution, and paired with indicators computable from the app's own data model. The current
version is a prototype with fictitious data for three cities in São Paulo state and has not been
evaluated with users; the paper documents the projected alignment, limitations and validation plan.

**Keywords:** Digital government. Citizen participation. Sustainable Development Goals. Mobile
application. Small municipalities. Android.

---

## 1 Introdução

A Agenda 2030 estabelece 17 Objetivos de Desenvolvimento Sustentável (ODS) e 169 metas (ONU, 2015).
Boa parte dessas metas só se realiza no nível local: é na prefeitura que se decide a manutenção de
vias, a coleta de resíduos, a iluminação de espaços públicos e a abertura de canais de participação.
Ao mesmo tempo, municípios pequenos dispõem de menos recursos para manter plataformas digitais
próprias, e o cidadão acaba recorrendo a canais fragmentados: vagas de emprego em murais e grupos de
mensagens, notícias espalhadas em diversos veículos locais e reclamações feitas por telefone ou
pessoalmente, sem protocolo nem retorno rastreável.

O *minhaCidade* propõe reunir esses três serviços em um aplicativo único. A premissa de negócio é
que **a prefeitura contrata o serviço** e o cidadão só consegue se cadastrar em cidades contratantes;
todo o conteúdo exibido é filtrado pelo município do usuário. Essa premissa é uma hipótese de
projeto, não uma parceria existente: o *minhaCidade* foi desenvolvido como trabalho acadêmico, cujo
objetivo era produzir uma solução voltada aos ODS, com apoio da instituição de ensino e sem municípios
parceiros, prefeituras contratantes ou jornais conveniados. As cidades "contratantes" do protótipo
(Lins, Promissão e Guaiçara) e todo o conteúdo são fictícios.

**Objetivo geral.** Projetar, implementar e analisar um aplicativo móvel municipal de vagas,
notícias e denúncias, explicitando sua contribuição aos ODS.

**Objetivos específicos.**
1. Especificar os requisitos funcionais e o modelo de dados de um serviço multi-município.
2. Implementar um protótipo funcional em Android com autenticação segura e ciclo de vida de denúncias.
3. Mapear funcionalidades a metas dos ODS e propor indicadores mensuráveis a partir dos dados do sistema.
4. Identificar limitações e definir um plano de avaliação com usuários e gestores públicos.

**Contribuições.** (i) Um artefato de software aberto a análise; (ii) uma matriz de rastreabilidade
entre funcionalidades e metas dos ODS, com critério explícito para evitar a reivindicação de impactos
não demonstrados; (iii) um conjunto de indicadores de monitoramento derivados do esquema de dados.

**Organização.** A Seção 2 apresenta a fundamentação; a 3, a metodologia; a 4, o artefato; a 5, o
alinhamento com os ODS; a 6, os resultados parciais; a 7, a discussão e as limitações; a 8, os
trabalhos futuros; a 9, a conclusão parcial.

## 2 Fundamentação

### 2.1 Agenda 2030 e a dimensão local

A Resolução A/RES/70/1 da Assembleia Geral da ONU (ONU, 2015) adota a Agenda 2030. O ODS 11
("Cidades e comunidades sustentáveis") e o ODS 16 ("Paz, justiça e instituições eficazes") tratam
diretamente da gestão urbana e das instituições; o ODS 8 ("Trabalho decente e crescimento
econômico") e o ODS 9 ("Indústria, inovação e infraestrutura") relacionam-se à inserção produtiva e
ao acesso a tecnologias de informação e comunicação.

A adaptação das metas globais ao nível subnacional é tratada na literatura como "localização" dos
ODS. O roteiro publicado em 2016 pela Global Taskforce of Local and Regional Governments, com o PNUD
e o ONU-Habitat, a organiza em conscientização, defesa de uma perspectiva subnacional nas estratégias
nacionais, implementação e monitoramento com dados locais (GLOBAL TASKFORCE; PNUD; ONU-HABITAT, 2016)
`[VERIFICAR: conferir no documento original]`. No Brasil, Eberhardt e Mantovaneli Junior (2024)
analisam o projeto "Localizando os ODS no Brasil", parceria iniciada em 2016 entre a Confederação
Nacional de Municípios e o PNUD para apoiar municípios na adaptação dos ODS ao nível local, e discutem
o papel da governança territorial. Muchagata et al. (2023) aplicaram um questionário a prefeituras,
sobretudo de municípios mais populosos e de alta vulnerabilidade socioeconômica, e obtiveram 38
respostas válidas sobre capacidade instalada, ações, barreiras e facilitadores para incorporar a
Agenda 2030. Akerman et al. (2024), em revisão narrativa, examinam os planos que cinco municípios
(Barcarena, Barueri, Francisco Morato, Santana de Parnaíba e São Paulo) relataram à ONU entre 2017 e
2023. Esses trabalhos tratam do planejamento e da governança municipais da Agenda 2030; a relação
entre a localização dos ODS e aplicativos de serviços ao cidadão, como o proposto aqui, não foi
encontrada nessa busca. `[VERIFICAR: os três artigos foram conhecidos por páginas das revistas e
resumos; ler os textos completos antes de submeter]`

### 2.2 Governo digital, transparência e participação

O arcabouço legal brasileiro sustenta os três módulos do aplicativo: a Lei de Acesso à Informação
(Lei nº 12.527/2011) garante o acesso a informações públicas; a Lei nº 13.460/2017 dispõe sobre a
participação, a proteção e a defesa dos direitos do usuário dos serviços públicos, incluindo
manifestações como reclamações e denúncias; a Lei nº 14.129/2021 trata dos princípios e instrumentos
do Governo Digital; e a Lei Geral de Proteção de Dados (Lei nº 13.709/2018) condiciona a coleta de
dados pessoais, como o CPF, a finalidade e necessidade legítimas.

### 2.3 Trabalhos relacionados

**Método e limites da busca.** Em 2026-11-03 fez-se uma busca exploratória, **não sistemática**, em
buscador web de uso geral, com consultas pelo nome de cada solução e pelo tema (zeladoria urbana e
participação cidadã, canais 156, vagas de emprego, notícias locais). Incluíram-se soluções brasileiras
ou de referência internacional, usadas por governos locais ou pela população, com página oficial ou
estudo acadêmico acessível; páginas de fabricantes e de prefeituras foram tratadas como
autodeclaração, e números de adoção ou de resolução de demandas não foram tomados como evidência de
eficácia. Não houve consulta a bases acadêmicas (Scopus, Web of Science, SciELO, Google Scholar),
nem período de corte, nem leitura integral de todos os textos. Por isso, nada abaixo prova a
ausência de outras soluções. `[A COLETAR: busca sistemática, com bases, strings de busca, período e
critérios de inclusão e exclusão registrados]`

**Zeladoria urbana e participação cidadã.** O FixMyStreet, do mySociety, é um serviço em que o
cidadão marca o problema em um mapa, o relato é enviado à autoridade responsável e publicado
na internet; o software é livre e há mais de 20 sites em diferentes países (MYSOCIETY, 2026). O Colab
reúne em um aplicativo o relato de problemas urbanos com foto, o apoio de outros usuários, consultas
públicas e serviços da prefeitura (COLAB, 2026). Carvalho (2018) estudou o Colab.re como dispositivo
de participação social na gestão de Paragominas (PA), e Rosa e Santos ([s.d.]) `[VERIFICAR: ano e
evento]` o citam, ao lado do e-Ouve, ao tomar como caso o sistema Reclama Muz, de Muzambinho (MG).
Os números que o fabricante exibe (mais de 1 milhão de cidadãos cadastrados e mais de 80% das demandas
publicadas resolvidas) são autodeclarados e não foram verificados. Entre os canais 156
municipais, o aplicativo 156+POA, de Porto Alegre, lançado em maio de 2021, reunia 93 serviços e
recursos em 2023 e abre solicitações de água e esgoto, iluminação pública, limpeza urbana e praças,
entre outras (PORTO ALEGRE, 2023); os 29 509 protocolos de janeiro a maio de 2023 são dado da
própria prefeitura.

**Vagas de emprego.** Em 2017 o Ministério do Trabalho lançou o aplicativo Sine Fácil, desenvolvido
pela Dataprev, que parece ter sido substituído pela consulta a vagas do Sine na Carteira de Trabalho
Digital `[VERIFICAR: informação obtida em notícias; confirmar em fonte oficial do gov.br]`. Não se
buscaram portais privados de emprego nem solução por município.

**Notícias locais.** O Atlas da Notícia, do Projor, mapeia os municípios sem veículos jornalísticos
(os "desertos de notícias") e é a referência encontrada para a oferta de notícia local no país
(PROJOR, [s.d.]) `[VERIFICAR: edição, URL e data de acesso]`. Não se buscaram agregadores
de notícias locais com a mesma profundidade.

**Posição do minhaCidade.** Nas soluções consultadas, os módulos aparecem separados (relato de
problemas urbanos, serviços da prefeitura, vagas do Sine, notícias), e o Colab e os canais 156
já reúnem serviços municipais em um só aplicativo. O que o minhaCidade explora é a combinação, em
um único acesso segmentado por município contratante, de vagas, notícias locais e denúncias com protocolo
e parecer, com a leitura explícita de cada função em relação aos ODS (Seção 5). Na busca realizada não
se encontrou solução com esses três módulos juntos, o que, dado o método acima, não é evidência de
ausência. Não se comparou desempenho, usabilidade nem resultados entre as soluções.

## 3 Metodologia

### 3.1 Design Science Research

Adota-se *Design Science Research* (HEVNER et al., 2004), seguindo as atividades de Peffers et al.
(2007): identificação do problema, definição dos objetivos, projeto e desenvolvimento do artefato,
demonstração, avaliação e comunicação. O desenvolvimento é incremental; cada ciclo é registrado no
Diário de Desenvolvimento (arquivo `artigo/diario.md`) e refletido neste texto, de modo que o artigo
evolui junto com o código.

### 3.2 Método de mapeamento para os ODS

Para cada funcionalidade:
1. descreve-se o mecanismo concreto no código (o que o sistema faz);
2. identifica-se a meta ODS cujo texto corresponde a esse mecanismo (usando o texto oficial das metas);
3. classifica-se a contribuição em **direta** (o mecanismo é o próprio instrumento da meta),
   **indireta** (o mecanismo é condição facilitadora) ou **potencial** (depende de adoção, de integração
   com a prefeitura ou de dados ainda inexistentes);
4. define-se um indicador calculável a partir dos dados do sistema;
5. registra-se explicitamente o que **não** é reivindicado.

Esse critério busca evitar o *SDG washing*: a associação decorativa de um produto a vários ODS sem
mecanismo nem medição.

### 3.3 Regras de integridade do texto

- Afirmações sobre o sistema devem corresponder ao código da versão indicada no cabeçalho.
- Nenhum dado empírico, estatística ou referência é incluído sem fonte verificada.
- Os dados do protótipo são fictícios (ver Seção 4.3) e não sustentam inferências.

## 4 O artefato: minhaCidade

### 4.1 Requisitos funcionais (versão 1.0)

Quadro: Requisitos funcionais da versão 1.0 | Fonte: elaborado pelo autor, a partir do código-fonte da versão 1.0.

| Id | Requisito | Módulo |
|----|-----------|--------|
| RF1 | Cadastro e login por CPF (validado pelos dígitos verificadores) e senha | Conta |
| RF2 | Edição de perfil e troca de senha; CPF imutável | Conta |
| RF3 | Cadastro restrito a cidades que contrataram o serviço | Conta / negócio |
| RF4 | Listar vagas da cidade, com busca por cargo, empresa ou bairro e filtro por tipo (CLT, Estágio, Temporário, PJ, Aprendiz) | Vagas |
| RF5 | Detalhar vaga e permitir candidatura por e-mail ou contato por telefone | Vagas |
| RF6 | Listar notícias da cidade, com filtro por jornal parceiro, e abrir a matéria no site do jornal | Notícias |
| RF7 | Registrar denúncia com categoria, endereço, descrição e foto opcional, gerando protocolo (`AAAA-NNNNN`) | Denúncias |
| RF8 | Acompanhar o status da denúncia (Aberta → Em análise → Resolvida) e ler pareceres da prefeitura | Denúncias |
| RF9 | Receber notificação e selo de não lidos quando a prefeitura responde | Denúncias |
| RF10 | Encaminhar a denúncia à prefeitura por e-mail ou telefone | Denúncias |
| RF11 | Informar, de forma opcional, a posição da denúncia: pela localização do aparelho (só após o cidadão tocar no botão e conceder a permissão) ou escolhendo um ponto no mapa; a posição pode ser removida antes do envio | Denúncias |
| RF12 | Ver no mapa as próprias denúncias que têm posição, abrir o cartão de uma denúncia e ir ao seu detalhe; no detalhe, "Ver no mapa" centraliza a denúncia | Denúncias |
| RF13 | Guardar no perfil um endereço residencial opcional (com coordenadas), digitado ou obtido pelo botão "Usar minha localização atual"; o endereço serve de atalho ("Usar meu endereço cadastrado") para preencher o local de uma nova denúncia | Conta / Denúncias |
| RF14 | Traduzir a posição obtida em endereço legível ("Rua, número - Bairro") pelo serviço de geocodificação do aparelho; se falhar, usar as coordenadas como texto. Na nova denúncia, "Usar minha localização" **substitui** o endereço pelo do local atual do cidadão; um ponto escolhido no mapa só preenche o campo se ele estiver vazio ou ainda contiver um endereço preenchido pelo app (texto digitado pela pessoa não é sobrescrito) | Conta / Denúncias |

### 4.2 Arquitetura

Aplicativo Android nativo em Java (Java 11; `minSdk` 26, `targetSdk` 37), com interface em Material
Components e navegação inferior por quatro abas (Vagas, Notícias, Denúncias, Perfil) implementadas
como *fragments*. A persistência é local, em SQLite (`Db`, esquema versão 4). A classe `Db` foi
escrita para que a camada de dados siga o contrato que uma futura API do site deverá seguir: toda
consulta de conteúdo recebe o identificador da cidade. Não há, na versão 1.0, servidor remoto.

O mapa usa a biblioteca osmdroid (versão 6.1.20) com a camada de ruas do OpenStreetMap (*Mapnik*),
o que dispensa chave de API e conta de cobrança, mas exige conexão para baixar os *tiles* e a
exibição da atribuição "© OpenStreetMap contributors" sobre o mapa (implementada). A posição do
aparelho é obtida pelo `LocationManager` do Android (sem serviços do Google), uma única vez, quando o
cidadão toca em "Usar minha localização". A escolha manual funciona movendo o mapa sob um alfinete
central e confirmando. A conversão de coordenadas em endereço usa a classe `Geocoder` do Android
(`EnderecoTexto`), executada fora da thread principal; ela depende de um serviço de geocodificação
do aparelho e de rede, e o aplicativo tem alternativa quando não há resposta (usar as coordenadas
como texto do endereço e avisar o cidadão para corrigi-lo). Buscas de endereço concorrentes (nova
posição, endereço cadastrado ou remoção da posição) tornam obsoleta a busca anterior.

A identidade visual do aplicativo é um alfinete de localização que contém o horizonte de uma cidade
sob um sol, sobre um fundo em degradê no azul da marca (`brand_primary`). O ícone de lançamento é um
ícone adaptativo vetorial (`ic_launcher_foreground`, `ic_launcher_background`), com versão
monocromática própria (`ic_launcher_monochrome`) para o Android 13 ou superior; o desenho é de
autoria do projeto e não foi avaliado com usuários. A mesma logo (`logo_app`) é exibida na tela de
entrada, acima do nome do aplicativo.

### 4.3 Modelo de dados

Seis tabelas: `cidades`, `usuarios`, `vagas`, `noticias`, `denuncias` e `pareceres`.
`vagas`, `noticias` e `denuncias` referenciam `cidades`; `denuncias` referencia o autor em
`usuarios`; `pareceres` referencia `denuncias` e guarda o status resultante e o indicador de leitura.
Chaves estrangeiras são ativadas na configuração do banco. Colunas de coordenadas (`latitude`,
`longitude`, reais e anuláveis) existem em `cidades` (centro aproximado, para abrir o mapa), em
`denuncias` (posição informada pelo cidadão) e em `usuarios` (endereço residencial opcional).

Os dados iniciais (*seed*) são **fictícios**: 3 cidades paulistas (Lins, Promissão e Guaiçara),
3 cidadãos de exemplo, 15 vagas (5 por cidade), 12 notícias (4 por cidade), 3 denúncias (todas com
coordenadas aproximadas, escolhidas apenas para demonstrar o mapa) e 3 pareceres. Empresas, jornais,
contatos e *links* são inventados (domínios `.example.com`).

### 4.4 Segmentação por município e modelo de contratação

Cada cidade tem a marca `contratada`. O cadastro e a troca de cidade no perfil só aceitam cidades
contratadas (erro `CIDADE_INDISPONIVEL`), e o login de usuário de cidade sem contrato é tratado
com mensagem específica. A consulta de vagas e de notícias é sempre filtrada por `cidade_id`.

### 4.5 Ciclo de vida da denúncia

1. O cidadão registra a denúncia (status `ABERTA`); o protocolo é derivado do ano e do identificador.
2. A prefeitura registra um parecer (`registrarParecer`), que, em uma única transação, insere o
   parecer e atualiza o status da denúncia (`EM_ANALISE` ou `RESOLVIDA`).
3. O sistema dispara notificação (canal "Pareceres das denúncias") e exibe selo numérico na aba.
4. Ao abrir a denúncia, os pareceres passam a *lidos*.

Na versão 1.0 o parecer é **simulado** por um botão disponível apenas em *builds* de depuração;
o mesmo caminho (`registrarParecer` + notificação) receberá respostas reais quando houver integração
com a prefeitura. Cada cidadão só consegue abrir as próprias denúncias.

### 4.6 Segurança e privacidade

- Senhas armazenadas com PBKDF2-HMAC-SHA256 (RFC 8018), 100 000 iterações, *salt* aleatório de
  16 bytes e comparação em tempo constante; política mínima de 8 caracteres.
- CPF validado pelos dígitos verificadores e exibido de forma mascarada no perfil.
- Permissão de notificações (Android 13+) solicitada sem bloquear o uso: sem ela, o aviso aparece
  apenas como selo no aplicativo.
- Permissão de localização solicitada **somente** quando o cidadão toca no botão de localização; a
  posição é opcional, pode ser removida antes do envio e a denúncia funciona sem ela. Quem recusa
  a permissão ainda pode marcar o ponto manualmente no mapa. O mapa de "ver" mostra apenas as
  denúncias do próprio cidadão, de modo coerente com a regra de que cada cidadão só abre as suas.
- Limitações de segurança estão discutidas na Seção 7.

## 5 Alinhamento com os ODS

### 5.1 Matriz funcionalidade × meta

Quadro: Matriz funcionalidade × meta ODS | Fonte: elaborado pelo autor, com base nas metas da Agenda 2030 (ONU, 2015).

| Funcionalidade (requisito) | Meta ODS (resumo do texto oficial) | Tipo | Mecanismo | Indicador proposto |
|---|---|---|---|---|
| Vagas locais por tipo (RF4–RF5) | **8.5** emprego pleno e produtivo e trabalho decente para todos, incluindo jovens | Indireta | Reduz o custo de busca por vagas formais da própria cidade | Vagas publicadas e acessos por cidade |
| Vagas de Estágio e Aprendiz (RF4) | **8.6** reduzir a proporção de jovens sem emprego, estudo ou capacitação (prazo original: 2020) | Indireta / potencial | Filtro dedicado a modalidades de entrada de jovens | Vagas Estágio+Aprendiz / total de vagas |
| Denúncias de buraco, sinalização e calçada (RF7–RF8) | **11.2** transporte seguro e acessível, segurança viária; **3.6** reduzir mortes e lesões no trânsito | Indireta | Canal para sinalizar riscos viários à prefeitura com protocolo | Denúncias por categoria; tempo até resolução |
| Denúncias de lixo ou entulho (RF7) | **11.6** gestão de resíduos urbanos | Indireta | Sinalização georreferenciável por endereço de acúmulo de resíduos | Denúncias de resíduos resolvidas / registradas |
| Denúncias de esgoto ou vazamento (RF7) | **6.3** qualidade da água e tratamento de efluentes; **6.4** eficiência no uso da água | Indireta | Sinalização de vazamentos e extravasamento de esgoto | Tempo até primeiro parecer nessa categoria |
| Denúncias de iluminação, árvore/poda e calçada (RF7) | **11.7** acesso universal a espaços públicos seguros, inclusivos, acessíveis e verdes | Indireta | Manutenção de espaços públicos a partir de relatos dos usuários | Taxa de resolução por categoria |
| Protocolo, status e pareceres (RF8–RF9) | **16.6** instituições eficazes, responsáveis e transparentes | **Direta** (para o canal) / potencial (para o efeito) | Protocolo rastreável e resposta registrada tornam a atuação da prefeitura verificável | Taxa de resolução; tempo até 1º parecer; % de denúncias com parecer |
| Posição da denúncia e mapa (RF11–RF12) | **16.6** (local preciso facilita a vistoria e a resposta); **11.3** planejamento urbano participativo | Potencial | Cada denúncia georreferenciada é um dado espacial de demanda; só vira insumo de planejamento se agregado e entregue à prefeitura (hoje o mapa é individual) | % de denúncias com posição; denúncias por bairro `[A COLETAR]` |
| Denúncia e retorno ao cidadão (RF7–RF9) | **16.7** tomada de decisão responsiva, inclusiva, participativa | Indireta | Canal de participação por demanda, não por deliberação | Denúncias por habitante cadastrado |
| Notícias de jornais locais (RF6) | **16.10** acesso público à informação | Indireta | Agrega o noticiário local por município | Notícias lidas / publicadas |
| Cidade contratante + jornais parceiros (RF3, RF6) | **17.17** parcerias público-privadas e com a sociedade civil | Indireta | Prefeitura contrata o serviço; veículos locais fornecem o conteúdo | Cidades contratantes; jornais parceiros |
| App gratuito ao cidadão, em celular (todos) | **9.c** ampliar o acesso às TIC; **10.2** inclusão de todos | Potencial (e com ressalva: ver 5.4) | Serviços públicos acessíveis por celular | Usuários cadastrados / população da cidade `[A COLETAR]` |

### 5.2 Categorias de denúncia e ODS

Quadro: Categorias de denúncia e ODS associados a priori | Fonte: elaborado pelo autor, com base nas categorias do aplicativo.

| Categoria no aplicativo | ODS / metas associadas |
|---|---|
| Buraco na rua | 11.2, 9.1 |
| Iluminação pública | 11.7 |
| Lixo ou entulho | 11.6 |
| Esgoto ou vazamento | 6.3, 6.4 |
| Calçada danificada | 11.2, 11.7, 10.2 (acessibilidade) |
| Semáforo ou sinalização | 11.2, 3.6 |
| Árvore ou poda | 11.7 |
| Outros | — (sem associação a priori) |

A associação é *a priori* (pelo tipo de problema). A contribuição só se concretiza se a prefeitura
resolver a demanda; o aplicativo contribui com a **visibilidade e a rastreabilidade**, não com a obra.

### 5.3 Indicadores calculáveis a partir do esquema

Os indicadores abaixo usam apenas tabelas e colunas existentes (SQLite). Em produção deverão ser
calculados sobre uma base central, já que o banco da versão 1.0 é local ao aparelho.

Quadro: Consultas SQL dos indicadores propostos | Fonte: elaborado pelo autor, a partir do esquema do banco local (`Db`).

```sql
-- Taxa de resolução por cidade (ODS 16.6)
SELECT cidade_id,
       ROUND(100.0 * SUM(status = 'RESOLVIDA') / COUNT(*), 1) AS pct_resolvidas
FROM denuncias GROUP BY cidade_id;

-- Tempo médio até o primeiro parecer, em horas (ODS 16.6)
SELECT d.cidade_id,
       ROUND(AVG((p.primeiro - d.criada_em) / 3600000.0), 1) AS horas_ate_parecer
FROM denuncias d
JOIN (SELECT denuncia_id, MIN(criado_em) AS primeiro
      FROM pareceres GROUP BY denuncia_id) p ON p.denuncia_id = d.id
GROUP BY d.cidade_id;

-- Demanda por categoria (ODS 3.6, 6.3, 11.2, 11.6, 11.7)
SELECT cidade_id, categoria, COUNT(*) AS total
FROM denuncias GROUP BY cidade_id, categoria;

-- Denúncias com posição geográfica (ODS 16.6, 11.3)
SELECT cidade_id,
       ROUND(100.0 * SUM(latitude IS NOT NULL AND longitude IS NOT NULL) / COUNT(*), 1)
         AS pct_georreferenciadas
FROM denuncias GROUP BY cidade_id;

-- Vagas voltadas a jovens (ODS 8.6)
SELECT cidade_id,
       SUM(tipo IN ('Estágio', 'Aprendiz')) AS vagas_jovens,
       COUNT(*) AS vagas_total
FROM vagas GROUP BY cidade_id;
```

Com os dados fictícios, 1 das 3 denúncias está resolvida, as 3 têm posição (por construção do
*seed*) e 3 das 15 vagas são de Estágio ou Aprendiz. Esses números servem apenas para demonstrar o cálculo e não permitem nenhuma inferência.

### 5.4 O que não é reivindicado

- O aplicativo **não mede** a qualidade do trabalho (ODS 8 "decente"): o salário é um texto livre e
  não há verificação das vagas nem das empresas.
- O aplicativo **não resolve** os problemas urbanos; depende da resposta da prefeitura (ODS 11).
- O aplicativo **não garante a veracidade** das notícias, apenas as agrega (ODS 16.10).
- O mapa **não é um painel público de transparência**: mostra só as denúncias do próprio cidadão.
  Um mapa aberto de demandas da cidade reforçaria 16.6, 16.7 e 11.3, mas exige decisão sobre
  privacidade e moderação e, por isso, não é reivindicado na versão atual.
- A posição informada **não é validada**: o aplicativo não confere se o ponto está dentro do
  município nem se corresponde ao endereço digitado.
- A versão atual exige smartphone Android (API 26+), o que **exclui** quem usa iOS ou não tem
  smartphone; para ODS 9.c e 10.2, a contribuição é potencial e pode ser negativa para quem está
  fora do público atendido.
- Não se reivindicam ODS que apenas aparecem como assunto do conteúdo fictício (ex.: notícias sobre
  saúde ou educação no *seed*).

## 6 Resultados parciais

Na versão 1.0, o protótipo implementa os requisitos RF1 a RF14 (Seção 4.1) com dados fictícios,
persistência local e simulação do parecer da prefeitura. Em 2026-11-02, a compilação de depuração
(`assembleDebug`) foi concluída e a tarefa `testDebugUnitTest` executou 13 testes unitários sem
falhas nem erros. Esses testes cobrem a validação de CPF, a consistência dos dados iniciais e a
formatação de endereços (e um teste de exemplo do *template*); **não há testes de interface nem de
integração**, e as telas, o mapa e a captura de localização ainda não têm verificação automatizada.
O fluxo do mapa foi verificado manualmente em um emulador Android (Pixel, API 34): abertura do
mapa pela lista de denúncias, cartão ao tocar no alfinete, navegação ao detalhe, escolha de um ponto
no mapa, preenchimento automático do endereço, registro de uma nova denúncia com posição e sua
exibição no mapa junto à denúncia anterior (com enquadramento automático dos dois pontos). O
caminho do GPS real (botão "Usar minha localização") **não** foi exercitado neste teste. A mudança
de 2026-11-03 (o botão passa a substituir o endereço pelo do local atual) foi verificada apenas por
compilação e pelos testes unitários existentes; **não** foi testada em dispositivo ou emulador
`[A COLETAR]`.
Na primeira execução o servidor público de *tiles* do OpenStreetMap respondeu HTTP 403 ("App is not
following the tile usage policy"), pois o aplicativo se identificava apenas pelo `applicationId`;
após passar a enviar um *User-Agent* que identifica o aplicativo (e limpar o cache local de *tiles*)
os mapas passaram a carregar. Não se isolou se a causa foi o *User-Agent* ou o cache de respostas de
erro. Trata-se de evidência, em escala de protótipo, da dependência de um serviço de terceiros.
[A COMPLETAR: capturas de tela para a versão final do artigo.]

**Avaliação com usuários:** não realizada. `[A COLETAR]`

## 7 Discussão e limitações

1. **Dados fictícios, sem servidor e sem parceiros.** Nenhum município ou jornal é parceiro do
   projeto, que conta apenas com o apoio da instituição de ensino; sem back-end, não há integração
   real com prefeituras nem com jornais; os indicadores da Seção 5.3 ainda não podem ser medidos.
2. **Armazenamento do CPF.** O CPF é gravado em texto claro na coluna `cpf` do banco local. Para
   produção, avaliar minimização, criptografia em repouso e base legal conforme a LGPD.
3. **Parâmetros de hash.** As 100 000 iterações de PBKDF2-HMAC-SHA256 estão abaixo da recomendação
   atual do OWASP para esse algoritmo (ordem de 600 000 iterações) `[VERIFICAR versão vigente]`.
   Reavaliar considerando o custo em dispositivos modestos.
4. **Canais por intenção.** O envio de e-mail e a ligação são delegados a aplicativos do aparelho
   (*intents*); não há confirmação de recebimento pela prefeitura.
5. **Fotos locais.** A foto da denúncia é um caminho local; sem servidor, não é compartilhada.
6. **Acessibilidade.** Não foi avaliada (leitores de tela, contraste, tamanho de fonte).
7. **Dados de localização.** As coordenadas (da denúncia e do endereço do perfil) revelam onde o
   cidadão mora ou circula e são gravadas sem criptografia no banco local. O tratamento exige
   finalidade, necessidade e base legal na LGPD `[VERIFICAR enquadramento]`; a coleta é opcional e
   iniciada pelo cidadão, mas a política de retenção e o compartilhamento com a prefeitura ainda
   não estão definidos.
   O uso do `Geocoder` envia as coordenadas a um serviço de geocodificação do sistema
   `[VERIFICAR qual provedor, conforme o aparelho]`, o que é mais um destino do dado de
   localização a descrever em uma política de privacidade.
8. **Mapa de terceiros.** Os *tiles* vêm dos servidores públicos do OpenStreetMap, o que expõe o
   endereço IP do aparelho a esse serviço e está sujeito à política de uso de *tiles* da
   fundação `[VERIFICAR]`; na prática, o protótipo já foi bloqueado uma vez pelo servidor (Seção 6).
   Para produção, é preciso um provedor contratado ou próprio, e não há
   funcionamento do mapa sem rede (exceto *tiles* já em cache).
9. **Precisão e conferência.** A precisão depende de o cidadão conceder localização precisa ou
   aproximada; não há verificação de que o ponto está dentro do município.
10. **Validade externa.** Três cidades do interior paulista não representam a diversidade de
   municípios brasileiros.

## 8 Trabalhos futuros

- API e painel web para a prefeitura (parecer real, gestão de vagas e de notícias).
- Mapa agregado para a prefeitura (denúncias por bairro e por categoria) e, se aprovado, mapa
  público e moderado de demandas; validação do ponto contra os limites do município; anexos de foto
  no servidor.
- Avaliação de usabilidade com a *System Usability Scale* (BROOKE, 1996) e entrevistas com gestores
  `[A COLETAR]`. Se houver participantes humanos, submeter a avaliação ao comitê de ética conforme a
  Resolução CNS nº 510/2016 `[VERIFICAR enquadramento]`.
- Piloto em uma cidade, medindo os indicadores da Seção 5.3 ao longo do tempo.
- Auditoria de acessibilidade e de proteção de dados.
- Versão para iOS ou web, para reduzir a exclusão digital.

## 9 Conclusão (parcial)

O *minhaCidade* organiza em um só aplicativo três serviços locais e, por sua estrutura (protocolo,
status, pareceres e segmentação por município), oferece um mecanismo concreto de transparência e
responsividade (ODS 16) e um canal de sinalização de problemas urbanos (ODS 11), com relações mais
indiretas com os ODS 3, 6, 8, 9, 10 e 17. A contribuição efetiva depende de adoção pela população e de resposta da
prefeitura, o que só pode ser afirmado após o piloto. Esta é uma versão parcial: a busca sistemática da Seção 2.3 e as
seções 6 e 8 serão completadas conforme o projeto avançar.

## Referências

AKERMAN, M.; MENDES, R.; OLIVEIRA, G. M. de; ALMEIDA, P. S. de; FRUTUOSO, M. F.; VIANA, C.; MORIYA, R. Y.; WALLERSTEIN, N. Municipal localization of the Sustainable Development Goals (SDGs) as a device to the continuous activation of the 2030 Agenda: a narrative review. **ABCS Health Sciences**, v. 49, e024307, 2024. DOI: 10.7322/abcshs.2023047.2307. `[VERIFICAR]`

BRASIL. **Lei nº 12.527**, de 18 de novembro de 2011. Regula o acesso a informações (Lei de Acesso à Informação).

BRASIL. **Lei nº 13.460**, de 26 de junho de 2017. Dispõe sobre participação, proteção e defesa dos direitos do usuário dos serviços públicos da administração pública.

BRASIL. **Lei nº 13.709**, de 14 de agosto de 2018. Lei Geral de Proteção de Dados Pessoais (LGPD).

BRASIL. **Lei nº 14.129**, de 29 de março de 2021. Princípios, regras e instrumentos para o Governo Digital.

BROOKE, J. SUS: a "quick and dirty" usability scale. In: JORDAN, P. W. et al. (org.). **Usability Evaluation in Industry**. London: Taylor & Francis, 1996. p. 189-194.

CARVALHO, T. L. M. **Computação urbana**: o uso do aplicativo móvel Colab.re como dispositivo de participação social na gestão da cidade de Paragominas-PA. 2018. 122 f. Dissertação (Mestrado em Gestão de Recursos Naturais e Desenvolvimento Local na Amazônia) – Núcleo do Meio Ambiente, Universidade Federal do Pará, Belém, 2018.

COLAB. **Todos os serviços da prefeitura em um só lugar**. Disponível em: https://www.colab.com.br/sou-cidadao. Acesso em: 3 nov. 2026.

EBERHARDT, F. L. M.; MANTOVANELI JUNIOR, O. A implementação dos Objetivos de Desenvolvimento Sustentável (ODS) nos municípios brasileiros: uma análise do projeto Localizando os Objetivos de Desenvolvimento Sustentável (ODS) no Brasil. **Desenvolvimento em Questão**, v. 22, n. 60, e15137, 2024. DOI: 10.21527/2237-6453.2024.60.15137.

GLOBAL TASKFORCE OF LOCAL AND REGIONAL GOVERNMENTS; PNUD; ONU-HABITAT. **Roadmap for localizing the SDGs**: implementation and monitoring at subnational level. 2016. `[VERIFICAR: URL, paginação e data de acesso]`

HEVNER, A. R.; MARCH, S. T.; PARK, J.; RAM, S. Design science in information systems research. **MIS Quarterly**, v. 28, n. 1, p. 75-105, 2004.

MORIARTY, K.; KALISKI, B.; RUSCH, A. **PKCS #5: Password-Based Cryptography Specification Version 2.1**. RFC 8018. IETF, 2017.

MUCHAGATA, M.; MIRANDA, W. D. de; FERNANDES, L. da M. M.; SANTOS, F. P. dos. Localizando objetivos do desenvolvimento sustentável: desafios de municípios brasileiros mais populosos e com alta vulnerabilidade socioeconômica para a implementação da agenda 2030. **Revista do Serviço Público**, v. 74, n. 4, p. 734-757, 2023. `[VERIFICAR: DOI e grafia dos nomes]`

MYSOCIETY. **FixMyStreet**. Disponível em: https://www.mysociety.org/community/fixmystreet/. Acesso em: 3 nov. 2026.

ONU. Assembleia Geral. **Transformando nosso mundo: a Agenda 2030 para o Desenvolvimento Sustentável**. Resolução A/RES/70/1, 2015.

OPENSTREETMAP FOUNDATION. **Tile Usage Policy** e licença dos dados (ODbL, "© OpenStreetMap contributors"). `[VERIFICAR: URLs e data de acesso]`

OWASP. **Password Storage Cheat Sheet**. `[VERIFICAR: URL e data de acesso]`

PEFFERS, K.; TUUNANEN, T.; ROTHENBERGER, M. A.; CHATTERJEE, S. A design science research methodology for information systems research. **Journal of Management Information Systems**, v. 24, n. 3, p. 45-77, 2007.

PORTO ALEGRE. Prefeitura Municipal. **Aplicativo 156+POA amplia funcionalidades e já reúne 93 serviços e recursos**. 7 jun. 2023. Disponível em: https://prefeitura.poa.br/smtc/noticias/aplicativo-156poa-amplia-funcionalidades-e-ja-reune-93-servicos-e-recursos. Acesso em: 3 nov. 2026.

PROJOR. **Atlas da Notícia**. `[VERIFICAR: edição, URL e data de acesso]`

ROSA, A. C. P.; SANTOS, P. C. **Engenharia de software aplicada à participação cidadã digital**: uma revisão bibliográfica sobre sistemas colaborativos urbanos. Muzambinho: IFSULDEMINAS, [s.d.]. `[VERIFICAR: evento, ano e URL]`

`[VERIFICAR]` Conferir paginação, grafia dos autores e dados de cada referência na fonte original antes de submeter.
