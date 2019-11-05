Current WIP

# Checkers AI
## Objectives
- Build an AI that can play checkers (draughts) at beginner/intermediate level
- Learn about Neural Networks and Genetic Algorithm

### How
Use a neural network to predict a score for state.

#### Neural Network
- 8x8 inputs (1 input by cell) in {-1 (opponent), 0 (empty), 1 (current player)} + 1 for current player in {-1 (opp), 1 (current)} => 65 inputs
- several layers of size [1, 64] => to be determined
- 1 output : the score in [?, ?]

#### Fitness (evaluation function)
- For a given NN, runs multiple games against other opponents (all?)
- At each step play each possible action and call the NN with the resulting state. Play the action that leads to highest-scored state. Opp also plays with its NN
- Update win-rate after a full game

#### Genetic algorithm
- Creates NNs of dimensions (see above)
- Evaluate each NN by winrate
- Applies elitism, selection, crossover and mutation on genes
- Repeats till over

#### Execution example

```
after 146 turns
- 2 - 2 - 2 - 2
2 - 2 - 2 - 2 -
- - - - - - - -
- - - - - - - -
- - - - - - - -
- - - - 1 - - -
- 1 - - - 1 - 1
1 - 1 - 1 - 1 -

[...]

- - - 2 - - - -
- - - - - - - -
- 2 - - - - - -
- - - - - - - -
- - - - - - - -
- - - - 1 - - -
- - - - - - - -
- - 2 - - - - -

- - - 2 - - - -
- - - - - - - -
- 2 - - - - - -
- - - - - - - -
- - - - - - - -
- - - - - - - -
- - - 1 - - - -
- - 2 - - - - -

- - - 2 - - - -
- - - - - - - -
- 2 - - - - - -
- - - - - - - -
- - - - - - - -
- - - - 2 - - -
- - - - - - - -
- - - - - - - -

winner is 2
Game stats over 100 samples: 48 - 96.18 - 207
```

#### Issues
- Since a NN only fights against other NN from the same generation, it's not enough to have an absolute score (and a usefull score progress graph) => a stable opponent (another checkers bot ?) would be necessary
