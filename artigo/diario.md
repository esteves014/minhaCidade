# Diário de desenvolvimento — minhaCidade

Registro de cada mudança relevante no projeto e do que ela exige no artigo (`artigo.md`).
Regra: a cada alteração no app, acrescentar uma linha aqui e atualizar as seções do artigo indicadas.

## Registro

| Data | Versão do app | Mudança | Seções do artigo afetadas | ODS / metas |
|------|---------------|---------|---------------------------|-------------|
| 2026-11-02 | 1.0 | **Estado inicial** (reconstruído do código, sem histórico anterior): login/cadastro por CPF, vagas, notícias, denúncias com pareceres e notificações, perfil, 3 cidades fictícias | Todas (rascunho 0.1) | 8.5, 8.6, 11.2, 11.6, 11.7, 16.6, 16.10, 17.17 |
| 2026-11-02 | 1.0 (banco v3→v4) | **Mapa e geolocalização das denúncias**: coordenadas opcionais em `denuncias` e `cidades`; "Usar minha localização" (GPS, sob demanda) e "Escolher no mapa" na nova denúncia; tela de mapa (osmdroid + OpenStreetMap) com as denúncias do cidadão; "Ver no mapa" no detalhe; atribuição OSM. Em paralelo, outra frente adicionou endereço residencial opcional (com coordenadas) ao perfil como atalho na nova denúncia (RF13). | 1 (resumo), 4.1 (RF11–RF13), 4.2, 4.3, 4.6, 5.1, 5.3, 5.4, 6, 7, 8, Refs | 16.6, 11.3 (potencial); 11.2, 11.6, 11.7 (indiretas, já mapeadas) |
| 2026-11-02 | 1.0 (banco v4) | **Endereço do cidadão e "usar minha localização" no perfil**: `usuarios` ganha `endereco`, `latitude`, `longitude` (opcionais); no Editar perfil, o botão "Usar minha localização atual" obtém a posição (sob demanda, com permissão) e a converte em "Rua, número - Bairro" pelo `Geocoder` do aparelho (`LocalizadorDeEndereco`, `EnderecoTexto`); se o geocoder falhar, usa as coordenadas como texto. Na nova denúncia, "Usar meu endereço cadastrado" e preenchimento automático do endereço a partir do GPS/mapa quando o campo está vazio. Testes: `EnderecoTextoTest`. Corpo do artigo atualizado (RF13–RF14, Seções 4.2, 6 e 7; 13 testes unitários, 0 falhas). | 4.1 (RF13), 4.3, 4.6, 7 (limitações: o `Geocoder` envia coordenadas a serviço do sistema; endereço e coordenadas em texto claro) | 11.2, 11.6 (indiretas, já mapeadas) |
| 2026-11-03 | 1.0 (banco v4) | **Endereço da denúncia pela localização atual**: na nova denúncia, "Usar minha localização" agora sempre substitui o endereço pelo do local atual (antes só preenchia se o campo estivesse vazio); se o geocoder falhar, usa as coordenadas como texto e avisa; ponto do mapa preenche só se o campo estiver vazio ou com endereço automático; busca obsoleta é descartada; título da seção passa a "Localização da ocorrência (opcional)". Verificado só por compilação e testes unitários (sem teste em aparelho). | 4.1 (RF14), 4.2, 6 | 16.6 (potencial, já mapeado) |
| 2026-11-03 | 1.0 (banco v4) | **Formato do artigo passa a seguir o template de TCC da Fatec Lins** (`template-artigo.docx`): capa, folha de rosto, folha de aprovação, sumário automático, título/autoria/afiliação, RESUMO e ABSTRACT, seções em Arial. `gerar-docx.js` agora preenche o template (antes gerava um Word aproximado da SBC do zero); `atualizar-sumario.ps1` atualiza o sumário via Word. Em `artigo.md`: linhas `Autor/Orientador/Curso/Semestre/ano` no cabeçalho, palavras-chave separadas por ponto, citações entre parênteses em caixa alta (ABNT), referências em ordem alfabética, legendas `Quadro:` com fonte nas tabelas e no bloco SQL. Sem mudança no app. | Cabeçalho (v0.4), Resumo/Abstract (palavras-chave), 3.1 e 8 (citações), Referências (ordem), 4.1, 5.1, 5.2 e 5.3 (legendas) | — |
| 2026-11-03 | 1.0 (banco v4) | **Contexto do projeto e revisão exploratória** (v0.5; sem mudança no app): informado pelo autor que o projeto é um trabalho acadêmico para produzir uma solução voltada aos ODS, com apoio só da faculdade e sem municípios parceiros (cidades "contratantes" e conteúdo do protótipo são fictícios). Seção 2.1 ganha referências sobre "localização dos ODS" (Global Taskforce/PNUD/ONU-Habitat 2016; Eberhardt e Mantovaneli Junior 2024; Muchagata et al. 2023; Akerman et al. 2024). Seção 2.3 ganha busca exploratória não sistemática (FixMyStreet, Colab, 156+POA, Sine, Atlas da Notícia) com limites declarados. | 1 (premissa), 2.1, 2.3, 7 (item 1), 9, Referências | 17.17 e 11 (contexto); nenhuma nova reivindicação |
| 2026-11-13 | 1.0 (banco v4) | **Logo e ícone do aplicativo** (v0.6): substitui o ícone padrão do Android Studio por alfinete com horizonte de cidade e sol, em fundo azul degradê (ícone adaptativo vetorial, com `ic_launcher_monochrome` própria); redesenhado a pedido do autor após a primeira versão, considerada simples demais; logo também exibida na tela de login (`logo_app`, `activity_login.xml`), verificada em emulador; fonte em `artigo/logo.svg`. Sem mudança funcional, de dados ou de segurança. | 4.2 (identidade visual) | nenhum (sem reivindicação de ODS) |
| 2026-11-13 | 1.0 (banco v4→v5) | **CRUD e banco reorganizados no padrão do projeto de referência (CRUD.zip)** (v0.7): `Db.java` (466 linhas) dividido em `DatabaseHelper` (esquema, constantes `COL_*` e operações SQL), repositórios por entidade, `ViewModel`s com `LiveData` e pacotes `database/model/repository/viewmodel/view`; telas e funcionalidades inalteradas. Banco recriado na versão 5 (só dados fictícios). Verificado em emulador: login, vagas, lista e detalhe de denúncias, parecer simulado, selo de não lidos, nova denúncia; 13 testes unitários sem falhas. Não verificados em emulador: notícias, cadastro, editar perfil e mapa. | 4.2, 4.3 (nomes das classes), 7 | nenhum (refatoração interna; sem reivindicação de ODS) |

## Pendências do artigo

- [x] Contexto do projeto (Seção 1): trabalho acadêmico, apoio da faculdade, sem municípios parceiros. Falta o nome da instituição/disciplina, se for citar
- [ ] Busca sistemática de trabalhos relacionados (Seção 2.3 tem só busca exploratória; falta Scopus/SciELO/Google Scholar, strings, período) e ler na íntegra os artigos citados em 2.1 e 2.3
- [ ] Capturas de tela e execução em dispositivo (Seção 6)
- [ ] Testes de interface/integração (existem 13 unitários: CPF, seed e formatação de endereço)
- [ ] Conferir todas as referências e itens `[VERIFICAR]`
- [ ] Preencher autor, orientador, curso e semestre/ano (linhas no início de `artigo.md`; hoje saem como `[A COMPLETAR]` na capa)
- [ ] Modelo da Fatec pede o texto todo com 15 a 20 páginas: a versão 0.5 tem 17 (4 de capa/folhas/sumário + 13 de corpo); conferir após completar a busca de 2.3 e as seções 6 e 8
- [ ] Decidir se haverá avaliação com usuários (e, se sim, ética em pesquisa)
- [ ] Capturas de tela do mapa para o artigo (fluxo já testado em emulador; GPS real ainda não testado)
- [ ] Decidir a política do mapa: manter só as denúncias do próprio cidadão ou criar mapa público moderado
- [ ] Definir retenção/compartilhamento de coordenadas (LGPD) e provedor de tiles para produção
- [x] Gerar versão .docx no modelo do TCC da Fatec Lins (`artigo.docx`)

## Pontos do código que viram "limitações" no artigo (a reavaliar a cada ciclo)

- CPF em texto claro no SQLite (`DatabaseHelper.java`, tabela `usuarios`)
- PBKDF2 com 100 000 iterações (`Senha.java`)
- Parecer simulado só em debug (`DenunciaDetalheActivity.java`)
- Sem servidor: indicadores não medíveis em escala
- Coordenadas em texto claro no SQLite (`denuncias`, `usuarios`); tiles do OSM público (`MapaActivity.java`)
