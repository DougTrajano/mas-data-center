# MAS Data Center

This project contains a Multi-Agent System (MAS) that simulates the data center environment.

It's my term project for the Multi-Agent Systems course at [PUCRS](https://www.pucrs.br/).

## Prerequisites

- JaCaMo environment ([see more](http://jacamo.sourceforge.net/))

## Demo

![](images/demo.gif)

## Screens

### World

In this window, we can see our environment as a grid of cells.

[![](images/world.png)](images/world.png)

- **<span style="color: red;">Red</span>**: Hardware issues.
- **<span style="color: orange;">Orange</span>**: Software issues.
- **<span style="color: dimgray;">Dark Gray</span>**: Servers.
- **White**: Free cells visited by the agents.
- **<span style="color: gray;">Light Gray</span>**: Free cells unvisited by the agents.
- **<span style="color: green;">Green</span>**: Command Center

The agents can move in the grid, when they are free they will be colored in blue, when they are carrying a hardware component or software they will be colored in gray.

### Logs

In this window, we can see the logs of each agent, the world, the manager, and so on.

[![](images/logs.png)](images/logs.png)

### Organisation Inspector

In this window, we can see the organisation of the agents.

[![](images/org_inspector.png)](images/org_inspector.png)

## How to run

If you are using Eclipse (with JaCaMo plugin), you can run the project double-clicking on the [mas_data_center.jcm](mas_data_center.jcm) file and selecting the "Run JaCaMo Application" menu.

## Changelog

See the [GitHub Tags](https://github.com/DougTrajano/pucrs-mas-data-center/tags) for a history of notable changes to this project.

## License

This software is licensed under the Apache 2.0 [LICENSE](LICENSE) © [DougTrajano](https://github.com/DougTrajano). 2021