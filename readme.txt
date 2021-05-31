ICHEA for Timetabling Problems - a discrete COP

Copyright (c) 2013, Anuraganand Sharma - All rights reserved.

This Matlab code is the implementation of GSGD proposed by Anuraganand Sharma in the paper:
A. Sharma, “Constraint Optimization for Timetabling Problems Using a Constraint Driven Solution Model,” 
Australasian Joint Conference on Artificial Intelligence, AI 2013: Advances in Artificial Intelligence. 

How to run:
1 - The program is old and relies on Swing Framework. You will need to add appframework-1.0.3.jar and swing-worker.jar into your project. Both jar files are available in the main folder. You may also need matlabcontrol-4.0.0.jar file.
2 - Run The program and wait for GUI screen to apprear. Select the following options in the given order:
- Nominal/Categorical or Ordinal Data
- Click Fill Test. You may change population size and total generations dyanmically during the program execution. 
- Click Get Data
- Select the timetable problem you would like to solve. 
- You may keep "solve by Constraint Satisfaction" option.
3 - If you want to Fitness Vs Generations plot then click "Draw Matlab (slow)" any time during execution.
4 - You may also change other parameters dynamically. Select ICHEA tab and choose the most appropriate combinations. 
5 - Debug tab is to see look some other output variables.  
