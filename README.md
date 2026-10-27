# NetResilience

**NetResilience** é um projeto acadêmico desenvolvido em Java para estudar algoritmos em grafos e analisar a resiliência de redes. O objetivo é identificar caminhos de menor custo e elementos cuja indisponibilidade pode comprometer a conectividade de uma rede.

Como cenário de aplicação, o projeto considera redes como sistemas de transporte: os nós representam pontos da rede, e as arestas representam conexões com um custo associado.

## Objetivos

- Implementar estruturas de dados para representar grafos.
- Encontrar caminhos de menor custo entre dois nós.
- Identificar pontes e pontos de articulação.
- Analisar como falhas em nós e conexões podem afetar a conectividade.
- Desenvolver futuramente uma interface gráfica para visualizar a rede e os resultados dos algoritmos.

## Funcionalidades implementadas

### Modelagem do grafo

O grafo é representado por uma lista de adjacência e utiliza as seguintes classes:

- **`Graph`**: gerencia os nós, as arestas e suas conexões.
- **`Node`**: representa um nó identificado por um ID.
- **`Edge`**: representa uma conexão entre dois nós, com identificador e peso.

A modelagem atual representa grafos não direcionados e ponderados. Os pesos devem ser finitos e não negativos. A estrutura permite arestas paralelas e não permite laços (arestas que conectam um nó a ele próprio).

### Caminhos mínimos — Dijkstra

A classe `Dijkstra` implementa o algoritmo de Dijkstra utilizando uma fila de prioridade (`PriorityQueue`).

O algoritmo pode:

- Encontrar o caminho de menor custo entre uma origem e um destino.
- Recuperar a sequência de nós e as arestas exatas utilizadas na rota.
- Calcular o custo total do caminho.
- Informar quando não existe uma rota entre os nós selecionados.

Os resultados são representados por `RouteResult`.

### Análise de conectividade — Tarjan

A classe `TarjanAnalysis` utiliza busca em profundidade (DFS), tempos de descoberta e valores *low-link* para identificar elementos críticos da rede:

- **Pontes (*bridges*)**: arestas cuja remoção aumenta o número de componentes conexos.
- **Pontos de articulação (*articulation points*)**: nós cuja remoção aumenta o número de componentes conexos.

Os resultados são representados por `CriticalElementsResult`.

## Tecnologias

- **Java 17**
- **Apache Maven**
- **JUnit 6**

## Estrutura do projeto

```text
net-resilience/
├── pom.xml
├── README.md
└── src/
    ├── main/java/com/henrique/netresilience/
    │   ├── algorithm/
    │   │   ├── connectivity/
    │   │   │   └── TarjanAnalysis.java
    │   │   └── shortestpath/
    │   │       └── Dijkstra.java
    │   └── model/
    │       ├── graph/
    │       │   ├── Graph.java
    │       │   ├── Node.java
    │       │   └── Edge.java
    │       └── result/
    │           ├── RouteResult.java
    │           └── CriticalElementsResult.java
    └── test/java/com/henrique/netresilience/
        ├── algorithm/
        │   ├── connectivity/
        │   │   └── TarjanAnalysisTest.java
        │   └── shortestpath/
        │       └── DijkstraTest.java
        └── model/graph/
            └── GraphTest.java
```

## Como executar

### Pré-requisitos

- JDK 17 ou superior.
- Apache Maven instalado e disponível no terminal.

### Clonar o repositório

```bash
git clone https://github.com/HenriqueFestraits/net-resilience.git
cd net-resilience
```

### Executar os testes

Na raiz do projeto, execute:

```bash
mvn test
```

O Maven compila o código e executa os testes automatizados. Os relatórios ficam disponíveis em `target/surefire-reports/`.

## Próximas etapas

O projeto continua em desenvolvimento. Entre as próximas etapas planejadas estão:

- Implementar a identificação explícita de componentes conexos.
- Desenvolver a simulação de falhas em nós e arestas.
- Comparar rotas e conectividade antes e depois de uma falha.
- Criar serviços para coordenar as análises e simulações.
- Desenvolver uma interface gráfica com JavaFX para visualizar a rede, destacar rotas e exibir elementos críticos.
- Ampliar os cenários de teste para diferentes configurações de grafos.

## Contexto acadêmico

Projeto desenvolvido para a disciplina de **Tópicos Avançados em Estruturas de Dados**, com foco na aplicação e análise de algoritmos em grafos.

---

*Projeto acadêmico em desenvolvimento.*
