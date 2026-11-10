# NetResilience

**NetResilience** é uma aplicação acadêmica em Java para análise de vulnerabilidade e resiliência de infraestruturas de telecomunicações, utilizando algoritmos em grafos. O sistema representa uma rede de distribuição de conectividade, permite simular indisponibilidades e avaliar o efeito estrutural de uma conexão redundante hipotética.

## Problema abordado

Falhas em equipamentos ou enlaces de uma infraestrutura de telecomunicações podem interromper a conectividade de regiões atendidas. O NetResilience oferece apoio à análise dessa infraestrutura, identificando elementos estruturalmente críticos e comparando os efeitos de cenários de falha ou de uma possível conexão adicional.

O sistema é uma ferramenta acadêmica de análise topológica. Ele **não configura equipamentos, não redireciona tráfego e não comprova a viabilidade física de uma nova fibra**. Em redes FTTH/PON, fatores como capacidade, portas disponíveis, divisores ópticos, orçamento óptico, tecnologia e rota física precisam ser avaliados separadamente.

## Modelo de representação

A infraestrutura é abstraída como um grafo não direcionado e ponderado:

- **Vértices (Node)**: centrais, equipamentos, caixas ou pontos de distribuição.
- **Arestas (Edge)**: enlaces que conectam dois pontos da infraestrutura.
- **Peso da aresta**: custo abstrato utilizado pelos algoritmos de caminho mínimo. Nos conjuntos de demonstração, os pesos são ilustrativos e não representam quilômetros nem medições reais.
- **Coordenadas X/Y**: metadados exclusivamente visuais, utilizados para posicionar os vértices na interface.

A estrutura permite arestas paralelas, úteis para representar conceitualmente enlaces independentes entre os mesmos pontos. A aplicação não modela capacidade, fluxo, tecnologia de acesso ou restrições físicas de engenharia. A quantidade de dispositivos dependentes é um metadado informado no CSV, usado para contextualizar a criticidade operacional; não é inferida pela topologia.

## Funcionalidades

### 1. Análise de caminhos mínimos — Dijkstra

A classe Dijkstra utiliza uma fila de prioridade para encontrar o caminho de menor custo entre uma origem e um destino. O resultado inclui a sequência de vértices, arestas e o custo total. Pesos devem ser finitos e não negativos.

### 2. Identificação de elementos críticos — Tarjan

A classe TarjanAnalysis utiliza busca em profundidade (DFS), tempos de descoberta e valores low-link para identificar:

- **Pontes**: enlaces cuja remoção aumenta o número de componentes conexos.
- **Pontos de articulação**: vértices cuja remoção aumenta o número de componentes conexos.

A análise considera grafos desconexos e arestas paralelas.

### 3. Componentes conexos — BFS

A classe ConnectedComponents utiliza busca em largura (BFS) para identificar os grupos de vértices que permanecem conectados entre si.

### 4. Simulação de falhas

A classe FailureScenario registra indisponibilidades de vértices e arestas. Quando um vértice fica indisponível, seus enlaces incidentes também deixam de ser utilizáveis.

A classe FailureSimulationService analisa a topologia original, constrói uma cópia sem os elementos indisponíveis e executa novamente Dijkstra, Tarjan e BFS. O grafo original é preservado.

Os resultados incluem rotas antes/depois, componentes conexos, pontes, pontos de articulação, disponibilidade de vértices e enlaces e cobertura do maior componente.

### 5. Relatório de avaliação da rede

O botão **Avaliar rede e gerar relatório** reúne em uma única saída a visão geral da topologia, componentes conexos, pontes, pontos de articulação, equipamentos com maior exposição operacional e sugestões de conexões redundantes.

As sugestões são geradas testando pares de vértices que ainda não possuem ligação direta. Para cada par, o serviço cria uma cópia do grafo, adiciona uma aresta hipotética e compara a quantidade de pontes e pontos de articulação. As sugestões que reduzem esses indicadores são ordenadas por melhoria estrutural e, em caso de empate, pela quantidade de dispositivos informada nos extremos.

A quantidade `dependent_devices` representa dispositivos dependentes informados pelo usuário. Ela não é usada por Tarjan/BFS e não deve ser interpretada como número exato de clientes afetados por uma falha, pois isso exigiria modelar a hierarquia de atendimento.

### 6. Avaliação de conexão redundante hipotética

A classe RedundancyAnalysisService permite avaliar o efeito estrutural de adicionar uma conexão entre dois vértices existentes.

O serviço:
1. Analisa pontes, pontos de articulação e componentes conexos da topologia original.
2. Cria uma cópia da infraestrutura, preservando os vértices, coordenadas, enlaces e pesos.
3. Adiciona à cópia o enlace hipotético informado.
4. Executa novamente as análises estruturais.
5. Retorna os indicadores antes e depois, incluindo a redução na quantidade de pontes e pontos de articulação.

A interface permite selecionar os dois pontos nos campos de origem e destino, informar um identificador e o peso abstrato da conexão e visualizar a comparação. A conexão proposta existe apenas na cópia utilizada para análise; ela não altera a infraestrutura carregada.

Uma redução no número de pontes ou pontos de articulação indica uma melhoria topológica segundo esses indicadores, mas não garante por si só que a conexão seja implementável ou que toda falha real seja tolerada.

## Interface gráfica

A interface utiliza JavaFX. Ela permite importar os arquivos CSV, selecionar origem e destino, marcar vértices e arestas como indisponíveis, visualizar rotas e avaliar uma conexão redundante hipotética.

Para executar:

```bash
mvn javafx:run
```

## Demonstração pelo terminal

A classe SimulationDemo demonstra cenários de falha. Também é possível carregar arquivos CSV e indicar origem, destino e falhas pela linha de comando.

```bash
mvn clean package
java -cp target/classes com.henrique.netresilience.demo.SimulationDemo
```

### 7. Topologia planejada e cenários integrados de falha

A aplicação mantém duas representações separadas:

- **Topologia original:** exatamente a infraestrutura importada dos CSVs.
- **Topologia planejada:** cópia da original que recebe os enlaces hipotéticos aceitos pelo usuário.

As conexões hipotéticas aparecem tracejadas em amarelo no Canvas e passam a integrar a topologia planejada. Elas podem ser selecionadas como enlaces indisponíveis e são consideradas pelo Dijkstra, Tarjan, BFS e pelas métricas da simulação de falhas.

O botão **Avaliar rede e gerar relatório** apresenta a análise da topologia planejada e, quando existem falhas selecionadas, acrescenta uma avaliação da topologia operacional restante. Assim, o relatório diferencia infraestrutura importada, conexões propostas e falhas simuladas.

Limpar as falhas não remove as conexões hipotéticas. Reimportar os CSVs reinicia o planejamento e descarta as propostas anteriores. A topologia original nunca é modificada por essas operações.

## Formato dos arquivos CSV

- nodes.csv: id,label ou id,label,x,y.
- edges.csv: id,source,target,weight.

Consulte data/CSV_FORMAT.md para detalhes e exemplos.

## Conjuntos de demonstração

- data/rede_densa: topologia em malha organizada em grade, útil para experimentar caminhos alternativos.
- data/rede_hibrida: topologia com núcleo central, ramificações regionais e conexões alternativas.

Os rótulos descrevem elementos hipotéticos de uma infraestrutura de telecomunicações. As topologias e pesos são dados sintéticos de demonstração, não o levantamento de uma operadora real.

## Tecnologias

- Java 17
- Apache Maven
- JavaFX 17
- JUnit 6

## Testes

Execute na raiz do projeto:

```bash
mvn test
```

Os testes cobrem a modelagem, os algoritmos, importação CSV, simulação de falhas e análise de conexões redundantes.

## Estrutura do projeto

```text
src/main/java/com/henrique/netresilience/
├── algorithm/              # BFS, Tarjan e Dijkstra
├── demo/                   # Demonstração por terminal
├── io/                     # Importação CSV
├── model/
│   ├── graph/              # Grafo, vértices e arestas
│   ├── result/             # Modelos de resultados
│   └── scenario/           # Cenários de falha
├── service/
│   ├── FailureSimulationService.java
│   └── RedundancyAnalysisService.java
└── ui/                     # Interface JavaFX
```

Projeto desenvolvido para a disciplina de **Tópicos Avançados em Estruturas de Dados**, com foco na aplicação de estruturas e algoritmos em grafos a um problema de resiliência de redes de telecomunicações.
