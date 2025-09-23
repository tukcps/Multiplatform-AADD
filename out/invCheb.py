import matplotlib.pyplot as plt
import numpy as np
import math
######### plot approximation
x=np.linspace(1.0,2.0,500)
yUpper= -0.25*x + 1.125 +0.12500000000000097
yApprox= -0.25*x + 1.125
yLower= -0.25*x + 1.125 -0.12500000000000097
plt.plot(x,yUpper,color='k')
plt.plot(x,yApprox,color='g')
plt.plot(x,yLower,color='k')
yUpper= -0.5*x + 1.4571067811865475 +0.04289321881345248
yApprox= -0.5*x + 1.4571067811865475
yLower= -0.5*x + 1.4571067811865475 -0.04289321881345248
plt.plot(x,yUpper,color='k')
plt.plot(x,yApprox,color='r')
plt.plot(x,yLower,color='k')


######## original function
y=1/x

plt.plot(x,y,color='b')

plt.xlabel('x')
plt.ylabel('y')
plt.grid()

plt.show()