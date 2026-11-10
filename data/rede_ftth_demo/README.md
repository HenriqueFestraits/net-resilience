# Rede FTTH demonstrativa

Topologia fictícia de um provedor FTTH, criada para exercitar a importação, a análise de conectividade, a identificação de pontos críticos, a simulação de falhas e o planejamento de enlaces redundantes no NetResilience.

## Arquivos
- nodes.csv: equipamentos, pontos de distribuição, CTOs, clientes diretamente associados e coordenadas visuais.
- edges.csv: enlaces e latência estimada de cada enlace em milissegundos.

## Organização
1. GW-01 representa o gateway de referência para a internet.
2. POP-01 a POP-04 representam pontos de presença/agregação. Formam um anel de backbone com uma ligação diagonal adicional.
3. O gateway possui dois uplinks, para POP-01 e POP-03.
4. OLT-01 a OLT-04 representam as OLTs das quatro regiões.
5. FD-01 a FD-08 representam pontos intermediários de distribuição.
6. CTO-01 a CTO-16 representam pontos terminais aos quais os clientes estão associados.

A camada de acesso é predominantemente hierárquica, enquanto a redundância está concentrada na camada de agregação. Isso permite simular falhas que aumentam a latência sem necessariamente interromper o acesso ao gateway.

## Interpretação dos clientes
O campo dependent_devices é preenchido somente nas CTOs e representa clientes diretamente associados àquela CTO. Os valores são exclusivos por CTO e não incluem clientes de outros pontos descendentes ou ancestrais. Assim, a soma das CTOs representa o total cadastrado nesta demonstração.

Os clientes são metadados dos vértices, não vértices individuais. Quando uma CTO deixa de ser alcançável a partir de GW-01 após uma falha, seus clientes são considerados sem caminho até o gateway. Isso não afirma que clientes isolados não possam se comunicar localmente; essa comunicação não é modelada pelo grafo atual.

## Pesos
Os pesos são valores sintéticos de latência estimada em milissegundos (ms) por enlace. Dijkstra soma os pesos para estimar a latência acumulada de um caminho. Não são medições reais e não modelam congestionamento, capacidade, protocolos de roteamento ou orçamento óptico.

## Cenários sugeridos
- Remover UPLINK-01: o gateway ainda possui caminho para a região pelo UPLINK-02 e pelo backbone, embora as rotas possam ficar mais longas.
- Remover BACKBONE-01: verificar se o anel e a ligação diagonal mantêm conectividade entre os POPs.
- Remover ACCESS-01: verificar o impacto sobre os clientes da OLT-01; a redundância do backbone não substitui automaticamente o enlace de acesso.
- Remover FEEDER-01: os clientes das CTOs 01 e 02 perdem o caminho até o gateway, enquanto os demais ramos permanecem conectados.

## Importação
Na aplicação, importe nodes.csv e edges.csv desta pasta como um par. Selecione GW-01 como origem nas análises de caminho. A aplicação ainda não identifica automaticamente o gateway pelo ID.
