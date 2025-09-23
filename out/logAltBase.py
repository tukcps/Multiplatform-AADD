import matplotlib.pyplot as plt
import numpy as np
import math
######### plot approximation
x=np.linspace(3.9999999999999996,8.0,2000)
yUpper= 0.25*x + 1.043035666027967 +0.0430356660279671
yApprox= 0.25*x + 1.043035666027967
yLower= 0.25*x + 1.043035666027967 -0.0430356660279671
plt.plot(x,yUpper,color='k')
plt.plot(x,yApprox,color='r')
plt.plot(x,yLower,color='k')

######## original function
y=np.log(x)/np.log(2.0)

plt.plot(x,y,color='b')

plt.xlabel('x')
plt.ylabel('y')
plt.grid()

plt.show()