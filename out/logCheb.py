import matplotlib.pyplot as plt
import numpy as np
import math
######### plot approximation


######## original function
x=np.linspace(1.0,2.0,500)
y=np.log(x)

plt.plot(x,y,color='b')

yUpper= 0.5*x + -0.40342640972002736 +0.0965735902799727
yApprox= 0.5*x + -0.40342640972002736
yLower= 0.5*x + -0.40342640972002736 -0.0965735902799727
plt.plot(x,yUpper,color='k')
plt.plot(x,yApprox,color='g')
plt.plot(x,yLower,color='k')

yChebUpper= 0.6931471805599453*x + -0.6633171299891405 +0.029830050570804817
yChebApprox= 0.6931471805599453*x + -0.6633171299891405
yChebLower= 0.6931471805599453*x + -0.6633171299891405 -0.029830050570804817
#plt.plot(x,yChebUpper,color='k')
#plt.plot(x,yChebApprox,color='r')
#plt.plot(x,yChebLower,color='k')



plt.xlabel('x')
plt.ylabel('y')
plt.grid()

plt.show()