import matplotlib.pyplot as plt
import numpy as np
import math
######### plot approximation
x=np.linspace(2.0,5.0,1500)
yUpper= 4.0*x + 0.5 +4.5
yApprox= 4.0*x + 0.5
yLower= 4.0*x + 0.5 -4.5
plt.plot(x,yUpper,color='k')
plt.plot(x,yApprox,color='r')
plt.plot(x,yLower,color='k')

yUpper= 7.0*x + -11.125 +1.125
yApprox= 7.0*x + -11.125
yLower= 7.0*x + -11.125 -1.125
plt.plot(x,yUpper,color='k')
plt.plot(x,yApprox,color='g')
plt.plot(x,yLower,color='k')

######## original function

y=np.power(x,2.0)

plt.plot(x,y,color='b')

plt.xlabel('x')
plt.ylabel('y')
plt.grid()

plt.show()