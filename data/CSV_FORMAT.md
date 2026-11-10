# Formato dos arquivos CSV

O NetResilience importa uma topologia a partir de dois arquivos CSV: um para os vértices e outro para os enlaces.

## nodes.csv

Cabeçalho aceito:

```csv
id,label,x,y
```

- id: identificador único do vértice.
- label: nome apresentado na interface.
- x e y: coordenadas visuais opcionais. Não participam dos algoritmos nem definem distâncias físicas.

Exemplo:

```csv
id,label,x,y
OLT-01,Central de distribuição 01,400,300
D-01,Caixa de distribuição Norte,230,120
D-02,Caixa de distribuição Sul,230,480
```

Também é aceito o formato sem coordenadas: id,label. Nesse caso, a interface posiciona os vértices automaticamente.

## edges.csv

Cabeçalho:

```csv
id,source,target,weight
```

- id: identificador único do enlace.
- source e target: IDs dos vértices nas extremidades.
- weight: peso numérico finito e não negativo utilizado como custo abstrato nos caminhos mínimos.

Exemplo:

```csv
id,source,target,weight
F-01,OLT-01,D-01,2.5
F-02,D-01,D-02,3.0
```

Os pesos dos arquivos de demonstração são sintéticos. Não devem ser interpretados como quilômetros, atenuação óptica ou capacidade real sem uma definição e calibração adicionais.

## Regras

- IDs de vértices e enlaces devem ser únicos dentro de seus respectivos arquivos.
- Toda extremidade de um enlace deve corresponder a um ID existente em nodes.csv.
- Laços (origem e destino iguais) não são aceitos.
- Arestas paralelas são aceitas e podem representar enlaces independentes entre os mesmos vértices.
- Campos devem ser separados por vírgula. Evite vírgulas dentro de rótulos.
