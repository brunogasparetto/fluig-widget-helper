# Widget Utilitária para a extensão do VS Code

Essa Widget, fluiggersWidget, fornece um conjunto de endpoints para auxiliar no desenvolvimento no Fluig usando a extensão do VS Code. Ela possibilita a importação de widgets e atualização de Evento de Workflow sem a necessidade de alterar a versão do Processo.

## Segurança

Para acessar os endpoints é necessário estar logado no Fluig e encaminhar os Cookies de autenticação junto na requisição.

Exceto pelo endpoint de Ping, todos os outros exigem que o usuário logado possua papel `admin` no Fluig, pois entendemos que são recursos que devem ter o máximo de restrição de acesso.

## Endpoints Criados

`GET /fluiggersWidget/api/ping` - Retorna um "pong" indicando que a widget está instalada;

`GET /fluiggersWidget/api/widgets` - Retorna um array com informações das widgets instaladas no Fluig;

`GET /fluiggersWidget/api/widgets/nome_arquivo.war` - Efetua o download do arquivo indicado;

`GET /fluiggersWidget/api/workflows/version?processId=?` - Pega a última versão do processo informado;

`PUT /fluiggersWidget/api/workflows/events` - Atualiza múltiplos eventos do processo na versão indicada;

## Contribuindo com o Projeto

Para compilar o projeto é necessário ter o Maven instalado e ter credenciais de acesso ao [Repositório do Fluig](https://nexus.fluig.com/) (para isso é necessário abrir um chamado no Suporte da TOTVS).

Você também pode contribuir abrindo issues indicando bugs e sugestões de melhorias.
