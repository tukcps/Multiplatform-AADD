import matplotlib.pyplot as plt
import numpy as np
import math
######### plot approximation
x=np.linspace(1.0,2.0,500)
yUpper= 0.4142135623730951*x + 0.5946699141100894 +0.008883476483185259
yApprox= 0.4142135623730951*x + 0.5946699141100894
yLower= 0.4142135623730951*x + 0.5946699141100894 -0.008883476483185259
#plt.plot(x,yUpper,color='k')
plt.plot(x,yApprox,color='r')
#plt.plot(x,yLower,color='k')
yUpper= 0.35355339059327373*x + 0.6767766952966368 +0.03033008588991149
yApprox= 0.35355339059327373*x + 0.6767766952966368
yLower= 0.35355339059327373*x + 0.6767766952966368 -0.03033008588991149
plt.plot(x,yUpper,color='k')
plt.plot(x,yApprox,color='g')
plt.plot(x,yLower,color='k')

######## original function
y=np.sqrt(x)

plt.plot(x,y,color='b')

plt.xlabel('x')
plt.ylabel('y')
plt.grid()

plt.show()