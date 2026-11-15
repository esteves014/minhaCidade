package com.example.minhacidade.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.example.minhacidade.model.Denuncia;

/**
 * Dados fictícios de Lins, Promissão e Guaiçara (SP). Jornais, empresas, bairros, contatos e links
 * são inventados e devem ser trocados por dados reais (ou por uma API) quando houver.
 *
 * Cada cidade tem um cidadão de exemplo para testar o login. Todos usam a senha DEMO_SENHA;
 * os CPFs são números de exemplo válidos, usados só nesta base fictícia.
 */
final class SeedData {
    static final long LINS = 1;
    static final long PROMISSAO = 2;
    static final long GUAICARA = 3;

    /** Senha dos cidadãos de exemplo. SALT e HASH foram gerados com auth.Senha (PBKDF2). */
    static final String DEMO_SENHA = "cidade123";
    static final String DEMO_SALT = "YNDnbJok01Cjn8u7gCoHZg==";
    static final String DEMO_HASH = "03KEPQkNyX9VeeIg3CzIou2QoKSQ+XA83ZvZphLAEfo=";

    private static final long HORA = 60L * 60 * 1000;

    private SeedData() {
    }

    static void popular(SQLiteDatabase db) {
        // Para simular uma cidade sem contrato, troque o último argumento para false.
        // Coordenadas aproximadas do centro de cada cidade (só para centralizar o mapa).
        cidade(db, LINS, "Lins", "ouvidoria@prefeitura-lins.example.com", "(14) 3000-0156", true, -21.6786, -49.7425);
        cidade(db, PROMISSAO, "Promissão", "ouvidoria@prefeitura-promissao.example.com", "(14) 3000-0256", true, -21.5342, -49.8586);
        cidade(db, GUAICARA, "Guaiçara", "ouvidoria@prefeitura-guaicara.example.com", "(14) 3000-0356", true, -21.6139, -49.8008);

        long maria = usuario(db, "52998224725", "Maria Souza", "maria@exemplo.com", LINS,
                "Rua das Flores, 100 - Centro");
        long joao = usuario(db, "11144477735", "João Pereira", "joao@exemplo.com", PROMISSAO,
                "Avenida Brasil, 250 - Centro");
        long ana = usuario(db, "12345678909", "Ana Lima", "ana@exemplo.com", GUAICARA,
                "Rua Dez, 30 - Vila Nova");

        vagasLins(db);
        vagasPromissao(db);
        vagasGuaicara(db);
        noticiasLins(db);
        noticiasPromissao(db);
        noticiasGuaicara(db);

        long buraco = denuncia(db, LINS, maria, "Buraco na rua", "Rua das Acácias, 120 - Jardim América",
                "Buraco grande no meio da pista, já causou danos em carros.", Denuncia.EM_ANALISE, 90,
                -21.6712, -49.7391);
        parecer(db, buraco, Denuncia.EM_ANALISE,
                "Recebemos sua denúncia e encaminhamos para a equipe de obras. A vistoria será feita nos próximos dias.",
                70, true);

        long iluminacao = denuncia(db, PROMISSAO, joao, "Iluminação pública", "Praça da Matriz - Centro",
                "Três postes apagados na lateral da praça, o local fica muito escuro à noite.", Denuncia.RESOLVIDA, 200,
                -21.5340, -49.8590);
        parecer(db, iluminacao, Denuncia.EM_ANALISE,
                "Sua denúncia foi encaminhada à equipe de iluminação pública.", 180, true);
        // Parecer ainda não lido: aparece como selo na aba e no item da lista ao entrar com este cidadão.
        parecer(db, iluminacao, Denuncia.RESOLVIDA,
                "A equipe trocou as lâmpadas dos três postes. Se o problema voltar, registre uma nova denúncia.",
                5, false);

        denuncia(db, GUAICARA, ana, "Lixo ou entulho", "Rua Sete, 45 - Vila Nova",
                "Entulho acumulado na calçada há mais de duas semanas.", Denuncia.ABERTA, 30,
                -21.6145, -49.8015);
    }

    // ---- Lins ----

    private static void vagasLins(SQLiteDatabase db) {
        vaga(db, LINS, "Atendente de loja", "Mercado Bom Preço", "CLT", "Centro", "R$ 1.800",
                "Atendimento ao cliente, reposição de produtos e organização da loja. Experiência anterior é um diferencial.\n\nBenefícios: vale-transporte e vale-alimentação.",
                "(14) 3000-1001", "rh@bompreco.example.com", 3);
        vaga(db, LINS, "Auxiliar administrativo", "Contábil Silva & Filhos", "CLT", "Jardim América", "R$ 2.100",
                "Rotinas de escritório, controle de documentos e atendimento telefônico. Conhecimento básico em Excel.",
                "(14) 3000-1002", "vagas@silvafilhos.example.com", 10);
        vaga(db, LINS, "Estagiário de Tecnologia da Informação", "Linense Tecnologia", "Estágio", "Centro", "R$ 1.200 + auxílio",
                "Suporte a usuários e manutenção de computadores. Cursando Sistemas de Informação ou áreas afins.",
                "(14) 3000-1003", "estagio@linensetec.example.com", 26);
        vaga(db, LINS, "Operador de produção", "Indústria Metalúrgica Norte", "CLT", "Distrito Industrial", "R$ 2.650",
                "Operação de máquinas e controle de qualidade. Turnos rotativos.",
                "(14) 3000-1004", "rh@metalnorte.example.com", 52);
        vaga(db, LINS, "Vendedor temporário (fim de ano)", "Magazine Central", "Temporário", "Centro", "R$ 1.650 + comissão",
                "Reforço de equipe para o período de festas, de novembro a janeiro. Possibilidade de efetivação.",
                "(14) 3000-1005", "contratacao@magazinecentral.example.com", 70);
    }

    private static void noticiasLins(SQLiteDatabase db) {
        noticia(db, LINS, "Jornal da Cidade de Lins", "Mobilidade",
                "Obras de recapeamento começam nesta semana no Centro",
                "Prefeitura anuncia intervenção em cinco ruas e orienta motoristas sobre desvios.",
                "A prefeitura iniciou o cronograma de recapeamento de cinco ruas do Centro. O trabalho será feito por trechos, no período da noite, para reduzir o impacto no trânsito.\n\nOs desvios serão sinalizados e agentes de trânsito vão orientar os motoristas. A previsão de conclusão é de 20 dias.",
                "https://jornaldacidade-lins.example.com/obras-centro", 2);
        noticia(db, LINS, "Diário Regional de Lins", "Economia",
                "Feira de empregos reúne mais de 30 empresas no sábado",
                "Evento gratuito oferece vagas para todos os níveis de escolaridade.",
                "Será realizada no sábado a feira de empregos da cidade, com mais de 30 empresas do comércio, da indústria e de serviços. A entrada é gratuita; leve currículo impresso e documento com foto.",
                "https://diarioregional-lins.example.com/feira-empregos", 8);
        noticia(db, LINS, "Jornal da Cidade de Lins", "Saúde",
                "Campanha de vacinação segue até o fim do mês",
                "Postos funcionam em horário estendido; confira os documentos necessários.",
                "A campanha continua em todas as unidades básicas de saúde, com horário estendido até as 19h. É necessário apresentar documento com foto e a caderneta de vacinação, quando houver.",
                "https://jornaldacidade-lins.example.com/vacinacao", 20);
        noticia(db, LINS, "Diário Regional de Lins", "Cidade",
                "Câmara aprova reforma da praça central",
                "Projeto prevê novo calçamento, iluminação em LED e área para feiras.",
                "Os vereadores aprovaram o projeto de revitalização da praça central, com calçamento acessível, iluminação em LED e uma área coberta para a feira livre. O início das obras depende da licitação.",
                "https://diarioregional-lins.example.com/praca-central", 30);
    }

    // ---- Promissão ----

    private static void vagasPromissao(SQLiteDatabase db) {
        vaga(db, PROMISSAO, "Auxiliar de produção rural", "Cooperativa Agrícola Regional", "CLT", "Zona Rural", "R$ 1.950",
                "Apoio nas atividades de campo e beneficiamento. Disponibilidade para horário flexível na safra.",
                "(14) 3000-2001", "rh@coopregional.example.com", 5);
        vaga(db, PROMISSAO, "Caixa de supermercado", "Supermercado Preço Justo", "CLT", "Centro", "R$ 1.750",
                "Operação de caixa, atendimento e fechamento. Escala 6x1.",
                "(14) 3000-2002", "vagas@precojusto.example.com", 12);
        vaga(db, PROMISSAO, "Jovem aprendiz administrativo", "Escritório Contábil Aliança", "Aprendiz", "Vila Nova", "Salário-aprendiz",
                "Para jovens de 16 a 22 anos cursando ou que concluíram o ensino médio. Apoio administrativo.",
                "(14) 3000-2003", "aprendiz@alianca.example.com", 40);
        vaga(db, PROMISSAO, "Motorista entregador", "Farmácia Saúde Já", "CLT", "Centro", "R$ 2.300",
                "Entregas de medicamentos na cidade. CNH categoria A ou B. Escala de fins de semana.",
                "(14) 3000-2004", "entregas@saudeja.example.com", 60);
        vaga(db, PROMISSAO, "Designer gráfico", "Agência Vitrine", "PJ", "Jardim Primavera", "A combinar",
                "Criação de peças para redes sociais e materiais impressos. Portfólio obrigatório.",
                "(14) 3000-2005", "criacao@agenciavitrine.example.com", 90);
    }

    private static void noticiasPromissao(SQLiteDatabase db) {
        noticia(db, PROMISSAO, "Gazeta de Promissão", "Cidade",
                "Prefeitura divulga calendário de manutenção de estradas rurais",
                "Equipes passam por cada região em dias definidos; produtores podem solicitar vistoria.",
                "A prefeitura divulgou o calendário de manutenção das estradas rurais. As equipes passarão por cada região em dias definidos e os produtores podem solicitar vistoria pelo telefone da secretaria.",
                "https://gazeta-promissao.example.com/estradas-rurais", 4);
        noticia(db, PROMISSAO, "Voz do Lago", "Esporte",
                "Torneio de pesca esportiva reúne participantes de toda a região",
                "Competição acontece no domingo e é aberta ao público.",
                "O torneio de pesca esportiva acontece no domingo, com participantes de várias cidades. A organização espera grande público e haverá praça de alimentação no local.",
                "https://vozdolago.example.com/torneio-pesca", 14);
        noticia(db, PROMISSAO, "Gazeta de Promissão", "Saúde",
                "Unidade de saúde amplia atendimento aos sábados",
                "Consultas de rotina e vacinação passam a ter horário aos sábados pela manhã.",
                "A unidade básica do Centro passa a atender aos sábados, das 8h às 12h, com consultas de rotina e vacinação. Não é necessário agendamento para vacinas.",
                "https://gazeta-promissao.example.com/saude-sabado", 28);
        noticia(db, PROMISSAO, "Voz do Lago", "Economia",
                "Comércio local prepara promoções para o fim de ano",
                "Lojistas se reúnem com a associação comercial para definir campanha.",
                "A associação comercial reuniu lojistas para definir a campanha de fim de ano, com sorteios e horário estendido nas semanas que antecedem o Natal.",
                "https://vozdolago.example.com/comercio-natal", 48);
    }

    // ---- Guaiçara ----

    private static void vagasGuaicara(SQLiteDatabase db) {
        vaga(db, GUAICARA, "Auxiliar de serviços gerais", "Prefeitura Digital Serviços", "Temporário", "Centro", "R$ 1.620",
                "Limpeza e conservação de prédios. Contrato temporário de três meses.",
                "(14) 3000-3001", "contratacao@pdservicos.example.com", 6);
        vaga(db, GUAICARA, "Balconista de padaria", "Padaria Pão Quente", "CLT", "Vila Nova", "R$ 1.700",
                "Atendimento no balcão e organização de vitrine. Horário a partir das 5h.",
                "(14) 3000-3002", "vagas@paoquente.example.com", 18);
        vaga(db, GUAICARA, "Estagiário administrativo", "Cerâmica Guaiçara", "Estágio", "Distrito Industrial", "R$ 1.100 + auxílio",
                "Apoio ao setor administrativo e financeiro. Cursando administração ou áreas afins.",
                "(14) 3000-3003", "estagio@ceramicag.example.com", 35);
        vaga(db, GUAICARA, "Ajudante de pedreiro", "Construtora Alicerce", "CLT", "Jardim Esperança", "R$ 2.000",
                "Auxílio em obras residenciais. Experiência desejável, não obrigatória.",
                "(14) 3000-3004", "obras@alicerce.example.com", 66);
        vaga(db, GUAICARA, "Recepcionista", "Clínica Bem-Estar", "CLT", "Centro", "R$ 1.850",
                "Recepção de pacientes, agendamentos e atendimento telefônico.",
                "(14) 3000-3005", "rh@bemestar.example.com", 80);
    }

    private static void noticiasGuaicara(SQLiteDatabase db) {
        noticia(db, GUAICARA, "Folha Guaiçarense", "Cidade",
                "Coleta de lixo terá novo horário a partir de segunda-feira",
                "Mudança busca reduzir o acúmulo de resíduos nas calçadas.",
                "A coleta de lixo passa a ser feita no período da manhã em todos os bairros a partir de segunda-feira. A prefeitura pede que os moradores coloquem o lixo na calçada somente no dia da coleta.",
                "https://folhaguaicarense.example.com/coleta-novo-horario", 5);
        noticia(db, GUAICARA, "Notícias do Interior", "Educação",
                "Escolas municipais abrem matrículas para o próximo ano",
                "Pais devem levar documentos do aluno e comprovante de residência.",
                "As matrículas para a rede municipal estão abertas até o fim do mês. Pais e responsáveis devem levar documento do aluno, comprovante de residência e carteira de vacinação.",
                "https://noticiasdointerior.example.com/matriculas", 22);
        noticia(db, GUAICARA, "Folha Guaiçarense", "Esporte",
                "Time local vence e avança no campeonato regional",
                "Vitória por 2 a 1 garantiu a vaga para a próxima fase.",
                "Jogando em casa, o time local venceu por 2 a 1 e garantiu vaga na próxima fase do campeonato regional. O gol da vitória saiu aos 43 minutos do segundo tempo.",
                "https://folhaguaicarense.example.com/campeonato", 46);
        noticia(db, GUAICARA, "Notícias do Interior", "Meio ambiente",
                "Campanha de plantio de árvores chega às escolas",
                "Alunos vão plantar mudas nas praças dos bairros.",
                "A campanha de plantio envolve as escolas municipais, com a participação dos alunos no plantio de mudas nas praças dos bairros. As mudas foram doadas por produtores locais.",
                "https://noticiasdointerior.example.com/plantio", 72);
    }

    // ---- Inserções ----

    private static void cidade(SQLiteDatabase db, long id, String nome, String email, String telefone,
                               boolean contratada, double latitude, double longitude) {
        ContentValues v = new ContentValues();
        v.put("latitude", latitude);
        v.put("longitude", longitude);
        v.put("id", id);
        v.put("nome", nome);
        v.put("uf", "SP");
        v.put("contratada", contratada ? 1 : 0);
        v.put("email_prefeitura", email);
        v.put("telefone_prefeitura", telefone);
        db.insert("cidades", null, v);
    }

    private static long usuario(SQLiteDatabase db, String cpf, String nome, String email, long cidadeId,
                                String endereco) {
        ContentValues v = new ContentValues();
        v.put("cpf", cpf);
        v.put("nome", nome);
        v.put("email", email);
        v.put("endereco", endereco);
        v.put("cidade_id", cidadeId);
        v.put("senha_salt", DEMO_SALT);
        v.put("senha_hash", DEMO_HASH);
        v.put("criado_em", System.currentTimeMillis());
        return db.insert("usuarios", null, v);
    }

    private static void vaga(SQLiteDatabase db, long cidadeId, String titulo, String empresa, String tipo,
                             String bairro, String salario, String descricao, String telefone, String email,
                             int horasAtras) {
        ContentValues v = new ContentValues();
        v.put("cidade_id", cidadeId);
        v.put("titulo", titulo);
        v.put("empresa", empresa);
        v.put("tipo", tipo);
        v.put("bairro", bairro);
        v.put("salario", salario);
        v.put("descricao", descricao);
        v.put("telefone", telefone);
        v.put("email", email);
        v.put("publicada_em", System.currentTimeMillis() - horasAtras * HORA);
        db.insert("vagas", null, v);
    }

    private static void noticia(SQLiteDatabase db, long cidadeId, String jornal, String categoria,
                                String titulo, String resumo, String corpo, String url, int horasAtras) {
        ContentValues v = new ContentValues();
        v.put("cidade_id", cidadeId);
        v.put("jornal", jornal);
        v.put("categoria", categoria);
        v.put("titulo", titulo);
        v.put("resumo", resumo);
        v.put("corpo", corpo);
        v.put("url", url);
        v.put("publicada_em", System.currentTimeMillis() - horasAtras * HORA);
        db.insert("noticias", null, v);
    }

    private static long denuncia(SQLiteDatabase db, long cidadeId, long usuarioId, String categoria,
                                 String endereco, String descricao, String status, int horasAtras,
                                 double latitude, double longitude) {
        ContentValues v = new ContentValues();
        v.put("latitude", latitude);
        v.put("longitude", longitude);
        v.put("cidade_id", cidadeId);
        v.put("usuario_id", usuarioId);
        v.put("categoria", categoria);
        v.put("endereco", endereco);
        v.put("descricao", descricao);
        v.put("status", status);
        v.put("criada_em", System.currentTimeMillis() - horasAtras * HORA);
        return db.insert("denuncias", null, v);
    }

    private static void parecer(SQLiteDatabase db, long denunciaId, String status, String texto,
                                int horasAtras, boolean lido) {
        ContentValues v = new ContentValues();
        v.put("denuncia_id", denunciaId);
        v.put("status", status);
        v.put("texto", texto);
        v.put("criado_em", System.currentTimeMillis() - horasAtras * HORA);
        v.put("lido", lido ? 1 : 0);
        db.insert("pareceres", null, v);
    }
}
