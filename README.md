# Books Service

API REST de gerenciamento de livros criada com Spring Boot. O serviço permite cadastrar livros, acompanhar o status de leitura, atualizar informações e publicar eventos quando um livro é concluído.

## Visão geral

O `books-service` é uma aplicação para controlar uma lista de leitura pessoal ou acadêmica. Ela combina:

- Spring Boot 3/4 para a API REST
- Spring Data JPA para persistência em PostgreSQL
- Redis para armazenamento de chaves de idempotência
- RabbitMQ para eventos assíncronos
- Flyway para migrações do banco de dados
- Actuator para health checks

## Funcionalidades

- Cadastro de livros
- Consulta por ID
- Listagem por status (`NOT_STARTED`, `READING`, `FINISHED`)
- Atualização de dados do livro
- Exclusão de livro
- Idempotência por `Idempotency-Key` para evitar duplicação de requisições
- Publicação de evento `book.finished` ao concluir um livro

## Stack tecnológica

- Java 21
- Maven
- Spring Boot 4.1.1
- PostgreSQL
- Redis
- RabbitMQ
- Docker / Docker Compose

## Estrutura do projeto

```text
books-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/studies/books_service/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── docker-compose.yml
├── pom.xml
├── mvnw
├── mvnw.cmd
├── postman/
│   └── books-service.postman_collection.json
└── README.md
```

## Pré-requisitos

- Java 21+
- Maven 3.9+
- Docker e Docker Compose
- Docker e Docker Compose para subir PostgreSQL, Redis e RabbitMQ localmente

## Configuração do ambiente

A aplicação usa os seguintes serviços de conexão (configurados em `src/main/resources/application.properties`):

- PostgreSQL: `localhost:5432`, banco `books`, usuário `books`
- Redis: `localhost:6379`
- RabbitMQ: `localhost:5672`, usuário e senha `books`

O arquivo `docker-compose.yml` sobe PostgreSQL, RabbitMQ e Redis para desenvolvimento local:

```bash
docker compose up -d
```

Os dados do PostgreSQL e do RabbitMQ são persistidos em volumes Docker. Para parar os serviços sem remover os dados:

```bash
docker compose down
```

## Executando a aplicação

```bash
./mvnw clean spring-boot:run
```

A API estará disponível em:

```text
http://localhost:8080
```

## Endpoints

### Criar livro

```http
POST /books
Header: Idempotency-Key: <chave-unica>
Content-Type: application/json
```

Body de exemplo:

```json
{
  "title": "The Hobbit",
  "author": "J. R. R. Tolkien"
}
```

### Consultar livro por ID

```http
GET /books/{id}
```

### Listar livros por status

```http
GET /books/status?status=READING&page=0&size=10
```

### Atualizar livro

```http
PUT /books/{id}
Content-Type: application/json
```

Body de exemplo:

```json
{
  "title": "The Hobbit",
  "author": "J. R. R. Tolkien",
  "status": "FINISHED",
  "rating": 5,
  "startDate": "2026-10-05T00:00:00.000Z",
  "finishDate": null
}
```

### Excluir livro

```http
DELETE /books/{id}
```

### Health check

```http
GET /actuator/health
```

## Exemplo com curl

```bash
curl -X POST http://localhost:8080/books \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: livro-001" \
  -d '{"title":"Clean Code","author":"Robert C. Martin"}'
```

## Idempotência

A criação de livros usa uma chave de idempotência enviada pelo header `Idempotency-Key`.

- A primeira requisição com a mesma chave e o mesmo payload é processada normalmente.
- Requisições repetidas com a mesma chave e o mesmo corpo retornam a resposta original.
- Se a chave for reutilizada com um payload diferente, a API rejeita a operação.

Os dados da idempotência são armazenados em Redis, com TTL de curta duração para o status em andamento e TTL maior para a resposta concluída.

## Eventos RabbitMQ

Quando um livro muda para `FINISHED`, a aplicação publica um evento de domínio e o listener registra a mensagem:

- Exchange: `books.exchange`
- Queue: `books.finished.queue`
- Routing key: `book.finished`

## Coleção Postman

A pasta `postman/` contém uma coleção exportada para testar os endpoints da API. Importar o arquivo:

```text
postman/books-service.postman_collection.json
```

## Testes

```bash
./mvnw test
```

## Observações

Essa aplicação foi criada como projeto de estudo para demonstrar uma combinação de padrões de API REST, persistência, controle de concorrência e comunicação assíncrona com RabbitMQ.
