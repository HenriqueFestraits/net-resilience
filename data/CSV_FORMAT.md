# Formato dos arquivos CSV

O NetResilience importa a rede por meio de dois arquivos: um arquivo de nós e outro de arestas.

## nodes.csv

O arquivo de nós aceita dois formatos.

### Sem coordenadas visuais

```csv
id,label
A,Estação Central
B,Estação Norte
C,Estação Sul
```

Nesse formato, a interface utiliza o posicionamento circular automático.

### Com coordenadas visuais

```csv
id,label,x,y
A,Estação Central,400,300
B,Estação Norte,230,120
C,Estação Sul,230,480
```

As coordenadas são usadas exclusivamente para definir a posição dos nós na interface.

- `x`: posição horizontal. Valores maiores deslocam o nó para a direita.
- `y`: posição vertical. Valores maiores deslocam o nó para baixo.
- Os valores podem ser inteiros ou decimais e também podem ser negativos.
- As coordenadas precisam ser números finitos.
- Todos os registros do mesmo arquivo devem seguir o cabeçalho escolhido.

A interface ajusta automaticamente a escala e centraliza a rede na área disponível, preservando a proporção entre os eixos.

## edges.csv

O formato das arestas não muda:

```csv
id,source,target,weight
E1,A,B,2.5
E2,B,C,3.0
```

O campo `weight` continua representando o custo utilizado pelo Dijkstra. Ele não precisa ter relação com a distância visual entre os nós.

## Exemplo

Com estes arquivos:

```csv
id,label,x,y
A,Estação Central,400,300
B,Estação Norte,230,120
```

```csv
id,source,target,weight
E1,A,B,2.5
```

a interface posiciona os nós de acordo com `x` e `y`, mas o custo da conexão continua sendo 2.5.
