import matplotlib.pyplot as plt
import numpy as np
import math
######### plot approximation


######## original function
x=np.linspace(6.999999999999999,13.0,3001)
y=np.log(x)

plt.plot(x,y,color='b')

yUpper= 0.07692307692307693*x + 1.4861989840276557 +0.07875037343388103
yApprox= 0.07692307692307693*x + 1.4861989840276557
yLower= 0.07692307692307693*x + 1.4861989840276557 -0.07875037343388103
plt.plot(x,yUpper,color='k')
plt.plot(x,yApprox,color='r')
plt.plot(x,yLower,color='k')

plt.xlabel('x')
plt.ylabel('y')
plt.grid()

plt.show()