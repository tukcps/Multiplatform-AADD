import matplotlib.pyplot as plt
import numpy as np
import math
######### plot approximation


######## original function
x=np.linspace(6.999999999999999,13.0,3001)
y=np.exp(x)

plt.plot(x,y,color='b')

yChebUpper= 73552.79314174865*x + -632217.4887263536 +118444.56989254155
yChebApprox= 73552.79314174865*x + -632217.4887263536
yChebLower= 73552.79314174865*x + -632217.4887263536 -118444.56989254155
plt.plot(x,yChebUpper,color='k')
plt.plot(x,yChebApprox,color='r')
plt.plot(x,yChebLower,color='k')

yUpper= 1096.6331584284576*x + 210788.6809993899 +217368.47994996063
yApprox= 1096.6331584284576*x + 210788.6809993899
yLower= 1096.6331584284576*x + 210788.6809993899 -217368.47994996063
plt.plot(x,yUpper,color='k')
plt.plot(x,yApprox,color='r')
plt.plot(x,yLower,color='k')

plt.xlabel('x')
plt.ylabel('y')
plt.grid()

plt.show()