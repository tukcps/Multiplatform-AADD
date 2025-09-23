import matplotlib.pyplot as plt
import numpy as np
import math
######### plot approximation


######## original function
x=np.linspace(1.0,2.0,500)
y=np.log(x)

plt.plot(x,y,color='b')

yUpper= 0.6931471805599453*x + -0.6633171299891405 +0.029830050570804817
yApprox= 0.6931471805599453*x + -0.6633171299891405
yLower= 0.6931471805599453*x + -0.6633171299891405 -0.029830050570804817
plt.plot(x,yUpper,color='k')
plt.plot(x,yApprox,color='r')
plt.plot(x,yLower,color='k')

plt.xlabel('x')
plt.ylabel('y')
plt.grid()

plt.show()