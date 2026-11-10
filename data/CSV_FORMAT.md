# Formato dos arquivos CSV

O NetResilience importa a topologia a partir de dois arquivos: `nodes.csv` e `edges.csv`.

## nodes.csv

Formatos aceitos:

- `id,label`
- `id,label,x,y`
- `id,label,dependent_devices`
- `id,label,dependent_devices,x,y`

O formato recomendado para a avaliação completa é:

```csv
id,label,dependent_devices,x,y
OLT-01,Central de distribuição,320,400,300
D-01,Caixa de distribuição Norte,120,220,150
D-02,Caixa de distribuição Sul,85,220,450
```

- `id`: identificador único do equipamento.
- `label`: nome apresentado na interface.
- `dependent_devices`: quantidade de dispositivos diretamente associados ao ponto. Para evitar dupla contagem, não inclua clientes já contabilizados em outros vértices. Deve ser um inteiro não negativo.
- `x` e `y`: coordenadas visuais opcionais; não participam dos algoritmos nem representam distâncias físicas.

Se o campo `dependent_devices` não for informado, o sistema assume zero internamente, mas preserva a distinção entre valor informado e não informado. Os algoritmos de grafos não usam essa quantidade; ela é utilizada para contextualizar a criticidade no relatório.

**Importante:** cada cliente deve ser contabilizado em apenas um vértice. Se clientes estiverem associados às CTOs, preencha `dependent_devices` nas CTOs e deixe zero nos equipamentos ancestrais. Assim, a soma representa clientes únicos.

## edges.csv

Cabeçalho:

```csv
id,source,target,weight
```

- `id`: identificador único do enlace.
- `source` e `target`: IDs dos equipamentos nas extremidades.
- `weight`: peso finito e não negativo utilizado por Dijkstra. Na rede FTTH demonstrativa, representa a latência estimada do enlace em milissegundos (ms); o caminho calculado soma essas estimativas.

Exemplo:

```csv
id,source,target,weight
F-01,OLT-01,D-01,2.5
F-02,OLT-01,D-02,3.0
```

Os pesos dos arquivos de demonstração são sintéticos; não são medições reais e não representam quilômetros, atenuação óptica ou capacidade.

## Regras

- IDs de nós e enlaces devem ser únicos em seus arquivos.
- Toda extremidade de enlace deve existir em `nodes.csv`.
- Laços não são aceitos; arestas paralelas são aceitas.
- Campos separados por vírgula; evite vírgulas dentro de rótulos.
- Os arquivos de demonstração usam contagens sintéticas de dispositivos apenas para testar a apresentação do relatório. Não são dados de operadoras reais.
