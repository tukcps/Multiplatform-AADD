import matplotlib.pyplot as plt
import numpy as np
import math
######### plot approximation


######## original function
x=np.linspace(1.0,2.0,500)
y=np.exp(x)

plt.plot(x,y,color='b')

yUpper= 4.670774270471606*x + -2.2404493224526822 +0.28795688044012047
yApprox= 4.670774270471606*x + -2.2404493224526822
yLower= 4.670774270471606*x + -2.2404493224526822 -0.28795688044012047
plt.plot(x,yUpper,color='k')
plt.plot(x,yApprox,color='r')
plt.plot(x,yLower,color='k')

yMinRUpper= 2.718281828459045*x + 0.9762462210062801 +0.9762462210062801
yMinRApprox= 2.718281828459045*x + 0.9762462210062801
yMinRLower= 2.718281828459045*x + 0.9762462210062801 -0.9762462210062801
plt.plot(x,yMinRUpper,color='k')
plt.plot(x,yMinRApprox,color='g')
plt.plot(x,yMinRLower,color='k')


plt.xlabel('x')
plt.ylabel('y')
plt.grid()

plt.show()
