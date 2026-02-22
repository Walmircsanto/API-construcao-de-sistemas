const { ApolloServer } = require("@apollo/server");
const { startStandaloneServer } = require("@apollo/server/standalone");

const gql = require("graphql-tag");

/*
Tudo que for adicionado aqui dentro, vai ser a porta de entrada do meu grafo
*/
// com a exclamação ele indica que e obrigatorio retorna uma String
const typeDefs = gql`
  type User {
    id: ID!
    name: String!
    email: String!
    age: Int
    balance: Float
  }

  type Product {
    name: String!
    preco: Float!
    desconto: Float
    precoComDesconto: Float
    produtoEmDestaque: String
  }

  type Query {
    getName: String!
    getAge: String
    getUser: User
    getProducts: Product
  }
`;

const resolvers = {
  User: {
    balance(user) {
      console.log(user);
      return user.salario_local;
    },
  },

  Query: {
    getName() {
      return "Retorna uma string qualquer";
    },
    getAge() {
      return "25";
    },
    getUser() {
      return {
        id: 1,
        name: "Matheus",
        email: "XXXXXXXXXXXXXX",
        age: 25,
        salario_local: 3000.5,
      };
    },

    getProducts() {
      return [
        {
          name: "Notebook",
          preco: 2500.5,
          desconto: 0.2,
        },
        {
          name: "Monitor",
          preco: 800.5,
          desconto: 0.1,
        },
      ];
    },
  },
};

const server = new ApolloServer({
  typeDefs,
  resolvers,
});

const { url } = startStandaloneServer(server, {
  listen: { port: 5000 },
}).then();

console.log("Server ready at: ", url);
