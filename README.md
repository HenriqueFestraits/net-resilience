# NetResilience

**NetResilience** é um projeto acadêmico desenvolvido em Java para analisar a resiliência de redes por meio de algoritmos em grafos. A aplicação permite encontrar caminhos de menor custo, identificar elementos críticos e simular falhas em nós e conexões, comparando o comportamento da rede antes e depois das indisponibilidades.

Como cenário de aplicação, o projeto considera redes como sistemas de transporte: os nós representam estações ou pontos de conexão, enquanto as arestas representam os trechos que ligam esses pontos. O peso de uma aresta representa o custo associado ao deslocamento.

## Funcionalidades implementadas

### Modelagem de grafos

A classe `Graph` representa um grafo **não direcionado e ponderado**, implementado com lista de adjacência.

- **`Graph`**: gerencia os nós, as arestas e as relações entre eles.
- **`Node`**: representa um nó identificado por ID e associado a um rótulo. Também pode armazenar coordenadas visuais opcionais (`x` e `y`), usadas somente pela interface gráfica.
- **`Edge`**: representa uma conexão identificada por ID, com origem, destino e peso.

A estrutura permite arestas paralelas, não permite laços (uma aresta que conecta um nó a ele próprio) e exige pesos finitos e não negativos. As coordenadas visuais dos nós são independentes dos pesos das arestas e não interferem nos algoritmos.

### Caminhos mínimos — Dijkstra

A classe `Dijkstra` implementa o algoritmo de Dijkstra utilizando uma fila de prioridade (`PriorityQueue`).

O algoritmo permite:

- Encontrar o caminho de menor custo entre um nó de origem e um nó de destino.
- Recuperar a sequência de nós e as arestas utilizadas na rota.
- Calcular o custo total do caminho.
- Indicar quando não existe uma rota entre os nós selecionados.

O resultado é representado por `RouteResult`. A busca retorna um `Optional<RouteResult>`, que fica vazio quando não há caminho disponível.

### Elementos críticos — Tarjan

A classe `TarjanAnalysis` utiliza busca em profundidade (DFS), tempos de descoberta e valores *low-link* para identificar:

- **Pontes (*bridges*)**: arestas cuja remoção aumenta o número de componentes conexos.
- **Pontos de articulação (*articulation points*)**: nós cuja remoção aumenta o número de componentes conexos.

O resultado é armazenado em `CriticalElementsResult`. A implementação considera grafos desconexos e trata corretamente a presença de arestas paralelas.

### Componentes conexos

A classe `ConnectedComponents` utiliza busca em largura (BFS) para identificar os componentes conexos do grafo.

O resultado, armazenado em `ConnectedComponentsResult`, permite consultar:

- A lista de componentes e os nós que pertencem a cada um.
- A quantidade de componentes conexos.
- Se o grafo é conexo.

### Cenários de falha

A classe `FailureScenario` permite registrar e restaurar indisponibilidades por meio dos IDs dos nós e das arestas.

Quando um nó fica indisponível, todas as arestas incidentes a ele também deixam de poder ser utilizadas. O cenário não modifica diretamente o grafo original.

### Simulação de falhas

A classe `FailureSimulationService` coordena os algoritmos para comparar a rede original com a rede afetada por um cenário de falhas.

Durante uma simulação, o serviço:

1. Valida se os elementos registrados no cenário existem no grafo original.
2. Calcula a rota, os componentes conexos e os elementos críticos da rede original.
3. Constrói uma cópia da rede sem os nós e as arestas indisponíveis.
4. Executa novamente as análises na rede afetada.
5. Consolida os resultados em um objeto `FailureSimulationResult`.

O grafo original é preservado. Assim, diferentes cenários podem ser simulados sem reconstruir ou modificar a rede de referência.

### Resultados da simulação

A classe `FailureSimulationResult` reúne:

- Rota original e rota após as falhas.
- Componentes conexos antes e depois do cenário.
- Pontes e pontos de articulação antes e depois.
- IDs dos nós e das arestas explicitamente registrados como indisponíveis.
- Disponibilidade da rota e indicação de perda da rota.
- Diferença entre o custo da rota afetada e o custo original, quando ambas existem.

### Importação de redes por CSV

A classe GraphCsvLoader importa a topologia da rede a partir de dois arquivos:

- `nodes.csv`: ID, rótulo e, opcionalmente, coordenadas visuais dos nós.
- `edges.csv`: ID, origem, destino e peso de cada aresta.

O arquivo de nós aceita os cabeçalhos `id,label` e `id,label,x,y`. Consulte também o [guia de formato dos CSVs](data/CSV_FORMAT.md). Quando as coordenadas estão presentes, a interface usa o posicionamento definido no CSV. Quando estão ausentes, a visualização mantém o posicionamento circular automático.

As coordenadas são valores numéricos relativos: X aumenta para a direita e Y aumenta para baixo. A interface ajusta escala e centralização para encaixar a rede na área disponível, preservando as proporções.

Exemplo:

```csv
id,label,x,y
A,Estação Central,400,300
B,Estação Norte,230,120
C,Estação Sul,230,480
```

O formato de edges.csv permanece:

```csv
id,source,target,weight
E1,A,B,2.5
E2,B,C,3.0
```

### Interface gráfica

A interface utiliza JavaFX e a classe GraphCanvas para visualizar a rede importada. É possível selecionar nós e arestas para criar cenários de falha, escolher origem e destino, executar a simulação e visualizar as rotas e os indicadores de resiliência.

Para iniciar a interface, execute:

```bash
mvn javafx:run
```

### Demonstração pelo terminal

A classe `SimulationDemo` monta uma rede de exemplo e executa dois cenários:

1. **Falha da conexão E1:** existe uma rota alternativa entre a origem e o destino, mas o custo do percurso aumenta.
2. **Falha da conexão E5:** a rede é dividida em dois componentes conexos e deixa de existir uma rota entre os pontos selecionados.

A demonstração também apresenta as rotas, os custos, os componentes conexos e os elementos críticos antes e depois de cada cenário. Ao final, verifica que a rede original continua com seus nós e arestas.

## Tecnologias

- **Java 17**
- **Apache Maven**
- **JavaFX 17**
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
    │   │   │   ├── ConnectedComponents.java
    │   │   │   └── TarjanAnalysis.java
    │   │   └── shortestpath/
    │   │       └── Dijkstra.java
    │   ├── demo/
    │   │   └── SimulationDemo.java
    │   ├── model/
    │   │   ├── graph/
    │   │   │   ├── Graph.java
    │   │   │   ├── Node.java
    │   │   │   └── Edge.java
    │   │   ├── result/
    │   │   │   ├── ConnectedComponentsResult.java
    │   │   │   ├── CriticalElementsResult.java
    │   │   │   ├── FailureSimulationResult.java
    │   │   │   └── RouteResult.java
    │   │   └── scenario/
    │   │       └── FailureScenario.java
    │   └── service/
    │       └── FailureSimulationService.java
    └── test/java/com/henrique/netresilience/
        ├── algorithm/
        │   ├── connectivity/
        │   │   ├── ConnectedComponentsTest.java
        │   │   └── TarjanAnalysisTest.java
        │   └── shortestpath/
        │       └── DijkstraTest.java
        ├── model/
        │   ├── graph/
        │   │   └── GraphTest.java
        │   ├── result/
        │   │   └── FailureSimulationResultTest.java
        │   └── scenario/
        │       └── FailureScenarioTest.java
        └── service/
            └── FailureSimulationServiceTest.java
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

O Maven compila o projeto e executa os testes automatizados. Os relatórios ficam disponíveis em `target/surefire-reports/`.

### Executar a demonstração

Para compilar o projeto e executar os testes antes da demonstração:

```bash
mvn clean package
```

Em seguida, execute a classe principal da demonstração:

```bash
java -cp target/classes com.henrique.netresilience.demo.SimulationDemo
```

## Próximas etapas

O projeto continua em desenvolvimento. Entre as próximas etapas planejadas estão:

- Ampliar os indicadores para comparar o impacto de diferentes cenários de falha.
- Expandir os cenários de demonstração e os testes automatizados.

## Contexto acadêmico

Projeto desenvolvido para a disciplina de **Tópicos Avançados em Estruturas de Dados**, com foco na implementação, aplicação e análise de algoritmos em grafos.

---

*Projeto acadêmico em desenvolvimento.*
