# Diário de desenvolvimento — minhaCidade

Registro de cada mudança relevante no projeto e do que ela exige no artigo (`artigo.md`).
Regra: a cada alteração no app, acrescentar uma linha aqui e atualizar as seções do artigo indicadas.

## Registro

| Data | Versão do app | Mudança | Seções do artigo afetadas | ODS / metas |
|------|---------------|---------|---------------------------|-------------|
| 2026-11-02 | 1.0 | **Estado inicial** (reconstruído do código, sem histórico anterior): login/cadastro por CPF, vagas, notícias, denúncias com pareceres e notificações, perfil, 3 cidades fictícias | Todas (rascunho 0.1) | 8.5, 8.6, 11.2, 11.6, 11.7, 16.6, 16.10, 17.17 |
| 2026-11-02 | 1.0 (banco v3→v4) | **Mapa e geolocalização das denúncias**: coordenadas opcionais em `denuncias` e `cidades`; "Usar minha localização" (GPS, sob demanda) e "Escolher no mapa" na nova denúncia; tela de mapa (osmdroid + OpenStreetMap) com as denúncias do cidadão; "Ver no mapa" no detalhe; atribuição OSM. Em paralelo, outra frente adicionou endereço residencial opcional (com coordenadas) ao perfil como atalho na nova denúncia (RF13). | 1 (resumo), 4.1 (RF11–RF13), 4.2, 4.3, 4.6, 5.1, 5.3, 5.4, 6, 7, 8, Refs | 16.6, 11.3 (potencial); 11.2, 11.6, 11.7 (indiretas, já mapeadas) |
| 2026-11-02 | 1.0 (banco v4) | **Endereço do cidadão e "usar minha localização" no perfil**: `usuarios` ganha `endereco`, `latitude`, `longitude` (opcionais); no Editar perfil, o botão "Usar minha localização atual" obtém a posição (sob demanda, com permissão) e a converte em "Rua, número - Bairro" pelo `Geocoder` do aparelho (`LocalizadorDeEndereco`, `EnderecoTexto`); se o geocoder falhar, usa as coordenadas como texto. Na nova denúncia, "Usar meu endereço cadastrado" e preenchimento automático do endereço a partir do GPS/mapa quando o campo está vazio. Testes: `EnderecoTextoTest`. Corpo do artigo atualizado (RF13–RF14, Seções 4.2, 6 e 7; 13 testes unitários, 0 falhas). | 4.1 (RF13), 4.3, 4.6, 7 (limitações: o `Geocoder` envia coordenadas a serviço do sistema; endereço e coordenadas em texto claro) | 11.2, 11.6 (indiretas, já mapeadas) |

## Pendências do artigo

- [ ] Nome, instituição e contexto do projeto (Seção 1 e cabeçalho)
- [ ] Revisão de literatura / trabalhos relacionados (Seção 2.3)
- [ ] Capturas de tela e execução em dispositivo (Seção 6)
- [ ] Testes de interface/integração (existem 13 unitários: CPF, seed e formatação de endereço)
- [ ] Conferir todas as referências e itens `[VERIFICAR]`
- [ ] Definir veículo/evento de submissão e adaptar o modelo (SBC, periódico, etc.)
- [ ] Decidir se haverá avaliação com usuários (e, se sim, ética em pesquisa)
- [ ] Capturas de tela do mapa para o artigo (fluxo já testado em emulador; GPS real ainda não testado)
- [ ] Decidir a política do mapa: manter só as denúncias do próprio cidadão ou criar mapa público moderado
- [ ] Definir retenção/compartilhamento de coordenadas (LGPD) e provedor de tiles para produção
- [ ] Gerar versão .docx no modelo SBC

## Pontos do código que viram "limitações" no artigo (a reavaliar a cada ciclo)

- CPF em texto claro no SQLite (`Db.java`, tabela `usuarios`)
- PBKDF2 com 100 000 iterações (`Senha.java`)
- Parecer simulado só em debug (`DenunciaDetalheActivity.java`)
- Sem servidor: indicadores não medíveis em escala
- Coordenadas em texto claro no SQLite (`denuncias`, `usuarios`); tiles do OSM público (`MapaActivity.java`)
