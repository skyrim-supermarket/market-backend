# Skyrim Supermarket

Implementação de um sistema de supermercado cujos produtos oferecidos são os itens encontrados no videogame "The Elder Scrolls V: Skyrim", produzido pela Bethesda Game Studios.

# Diagrama Entidade-Relacionamento

Segue o ERD do banco de dados com todas as tabelas e como elas se relacionam:

![ERD](https://github.com/user-attachments/assets/b903a4a1-55c2-4c3e-8746-0c933b985d80)

# Dependências

* Open JDK 17

# Principais tecnologias utilizadas

* Kotlin/Ktor
* PostgreSQL (dados reais) e H2 (testes)

# Como executar

Clone o repositório:

```
git@github.com:skyrim-supermarket/market-backend.git
cd market-backend
```

No seu terminal, rode o comando a seguir para fazer a build do servidor:

```
./gradlew build
```

E para subir o servidor, rode esse comando:

```
./gradlew run
```

Com isso, o servidor deverá estar ativo em ```http://localhost:8080```.

# Sobre o banco de dados

Não utilizamos Docker para disponibilizar um sistema prévio de banco de dados em um container. Com isso, será necessário criar um servidor de banco de dados na sua pŕopria máquina e que esteja de acordo com as especificações da conexão estabelecidas no arquivo ```./src/main/kotlin/plugins/Databases.kt``` (ou com a sua própria especificação, caso mude). Para criar tal servidor, uma ferramenta que recomendamos é o [pgAdmin](https://www.pgadmin.org/).

# Funcionalidades

* Listagem de itens com seus atributos;
* Filtragem e ordenação por nome e faixa de preço;
* Carrinho de compras;
* Listagem de compras passadas;
* Compras online (via um entregador) e in loco (via um caixa)
* Administrador: listagem, adição, edição e remoção de produtos e funcionários;

# Licença

Todo o projeto está sob a licença GPL 3.0.

# Disclaimer

O desenvolvimento desse projeto contou com auxílio de ferramentas de inteligência artificial.
