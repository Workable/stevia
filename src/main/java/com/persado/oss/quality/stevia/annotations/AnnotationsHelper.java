package com.persado.oss.quality.stevia.annotations;

/*
 * #%L
 * Stevia QA Framework - Core
 * %%
 * Copyright (C) 2013 - 2014 Persado
 * %%
 * Copyright (c) Persado Intellectual Property Limited. All rights reserved.
 *  
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *  
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *  
 * * Neither the name of the Persado Intellectual Property Limited nor the names
 * of its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *  
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import com.persado.oss.quality.stevia.selenium.core.SteviaContext;
import com.persado.oss.quality.stevia.selenium.core.WebController;
import com.persado.oss.quality.stevia.selenium.core.controllers.SteviaWebControllerFactory;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Component
public class AnnotationsHelper implements ApplicationContextAware {
	public static Logger LOG = LoggerFactory.getLogger(AnnotationsHelper.class);
	
	private static final ThreadLocal<Deque<WebController>> controllerStack = new ThreadLocal<Deque<WebController>>() {
		@Override
		protected Deque<WebController> initialValue() {
			return new LinkedList<WebController>();
		};
	};

    private static final ThreadLocal<Map<String, WebController>> controllers = new ThreadLocal<Map<String, WebController>>() {
        @Override
        protected Map<String, WebController> initialValue() {
            return new HashMap<String, WebController>();
        };
    };

	public static void disposeControllers() {
		for (WebController controller : controllers.get().values()) {
			LOG.info("Removing {} in SteviaContext.clean - disposeControllers()", controller);
			try {
				controller.quit();
			} catch (WebDriverException wde) {
				LOG.warn("Exception caught calling controller.quit(): \"" + wde.getMessage() + "\" additional info: " + wde.getAdditionalInformation());
			}

		}
		controllers.get().clear();
		Deque<WebController> cache = controllerStack.get();
		while (cache.peek() != null) {
			LOG.warn("test ends while controllers still masked - will clear the masked controller also");
			WebController controller = cache.pop();
			try {
				controller.quit();
			} catch (WebDriverException wde) {
				LOG.warn("Exception caught calling controller.quit(): \""+wde.getMessage()+"\" additional info: "+wde.getAdditionalInformation());
			}
		}
		cache.clear();
	}

    public static void disposeController(Class<? extends WebController> requestedControllerClass) {
        try {
                controllers.get().get(requestedControllerClass.getCanonicalName()).quit();
            } catch (WebDriverException wde) {
                LOG.warn("Exception caught calling controller.quit(): \""+wde.getMessage()+"\" additional info: "+wde.getAdditionalInformation());

        }
        controllers.get().remove(requestedControllerClass.getCanonicalName());
        controllerStack.get().remove(requestedControllerClass);
    }

	public Map<String,WebController> getControllers() {
		return controllers.get();
	}

	private ApplicationContext context;
	
	
	public void maskExistingController(Method m) throws Throwable {
		StopWatch watch = new StopWatch("Controller Mask");
		try {
			
			RunsWithController rw = 
					(m.getDeclaringClass().getAnnotation(RunsWithController.class) != null) ? 
								m.getDeclaringClass().getAnnotation(RunsWithController.class) : 
								m.getAnnotation(RunsWithController.class);
			if (null != rw) {
				watch.start("Controller masking");
				Class<? extends WebController> requestedControllerClass = rw.value();
				controllerMask(requestedControllerClass);
				watch.stop();
			} else {
				throw new IllegalStateException("unable to find an entry for the annotation!");
			}
		} finally {
			if (watch.isRunning()) {
				watch.stop();
			}
			LOG.info(watch.shortSummary());
		}
	}
	
	
	/**
	 * mask existing controller with requestedControllerClass
	 * @param requestedControllerClass
	 */
	private void controllerMask(Class<? extends WebController> requestedControllerClass) throws InterruptedException, ExecutionException, TimeoutException, MalformedURLException {
		WebController currentControllerObj = SteviaContext.getWebController();
		Class<? extends WebController> currentControllerClass = currentControllerObj.getClass();
		String curControllerKey = currentControllerClass.getCanonicalName();
		String reqControllerKey = requestedControllerClass.getCanonicalName();

		if (!controllers.get().containsKey(curControllerKey)) {
			controllers.get().put(curControllerKey, currentControllerObj);
		}
		
		
		//check if requested controller is different from the currently running
		if (!curControllerKey.startsWith(reqControllerKey)) {
			WebController replacer = null;
			if (controllers.get().containsKey(reqControllerKey)) { // we have one
				replacer = controllers.get().get(reqControllerKey);
			} else {
				replacer = SteviaWebControllerFactory.getWebController(context, requestedControllerClass);
				controllers.get().put(reqControllerKey, replacer);
				LOG.debug("Controller {} not found in cache, created new ", reqControllerKey);
			}
			controllerStack.get().push(currentControllerObj);
			SteviaContext.setWebController(replacer);
		} else {
			LOG.warn("Controller requested is the currently used one. No masking done!");
		}
	}
	
	public void controllerUnmask() throws Throwable {
		Deque<WebController> cache = controllerStack.get();
		if (cache.peek() != null) {
			SteviaContext.setWebController(cache.pop());
			LOG.debug("Controller unmasked");
		} else {
			LOG.warn("Controller was not masked. It is optional anyway in @RunsConditionsWithController.");
		}
	}
	
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		context = applicationContext;
	}


	public void maskAndExecPreconditions(Method m, Object testObject) throws Throwable {
		StopWatch watch = null;
		// need flag so we do not pop() the stack too much
		boolean masked = false;
		try {
			
			Preconditions rw = m.getAnnotation(Preconditions.class);
			if (null != rw) {
				if (rw.value().length == 0) {
					LOG.info("no preconditions found - masking not necessary");
					return;
				}
				watch = new StopWatch("Pre-Conditions execution");
				Class<? extends WebController> requestedControllerClass = rw.controller();
				watch.start("PreConditions run");

				if (requestedControllerClass != VoidController.class) {
					controllerMask(requestedControllerClass);
					masked = true;
				} else {
					LOG.debug("Empty controller found, no controller masking for pre/post conditions");
				}
				
				for (String methodName : rw.value()) {
					LOG.info("Executing preCondition method {} for method {}", methodName, m.getName());
					executeMethod(m, testObject, methodName);
				}
			} else {
				throw new IllegalStateException("unable to find an entry for the annotation!");
			}
		} finally {
			if (masked) controllerUnmask();
			if (null != watch && watch.isRunning()) {
				watch.stop();
				LOG.info(watch.shortSummary());
			}
		}
	}


	private Method findMethodByName(String methodName, Class<?> clazz) {
		Method toExecute = null;
		if (null == clazz || clazz == Object.class) {
			LOG.error("Cannot find method {} in class {} and all it's parents, will return null",methodName, clazz.getName());
			return null;
		}
		LOG.debug("Looking for method {} in class {} ",methodName, clazz.getName());
		for (Method method : clazz.getMethods()) {
			if (method.getName().equals(methodName)) {
				LOG.debug("found method object for method {}",methodName);
				toExecute = method;
				break;
			}
		}
		
		if (toExecute == null) {
			return findMethodByName(methodName, clazz.getSuperclass());
		} else {
			return toExecute;
		}
	}


	public void maskAndExecPostconditions(Method m, Object testObject) throws Throwable {
		StopWatch watch = null;
		// need flag so we do not pop() the stack too much
		boolean masked = false;
		
		try {
			
			Postconditions rw = m.getAnnotation(Postconditions.class);
			if (null != rw) {
				if (rw.value().length == 0) {
					LOG.info("no postconditions found - masking not necessary");
					return;
				}
				watch = new StopWatch("Post-Conditions execution");
				Class<? extends WebController> requestedControllerClass = rw.controller();
				watch.start("PostConditions run");

				if (requestedControllerClass != VoidController.class) {
					controllerMask(requestedControllerClass);
					masked = true;
				} else {
					LOG.debug("Empty controller found, no controller masking for pre/post conditions");
				}
				
				for (String methodName : rw.value()) {
					LOG.info("Executing postCondition method {} for method {}", methodName, m.getName());
					executeMethod(m, testObject, methodName);
				}
			} else {
				throw new IllegalStateException("unable to find an entry for the annotation!");
			}
		} finally {
			if (masked) controllerUnmask();
			if (null != watch && watch.isRunning()) {
				watch.stop();
				LOG.info(watch.shortSummary());
			}
		}
	}


	private void executeMethod(Method m, Object testObject, String methodName)
			throws IllegalAccessException, InvocationTargetException {
		try {
			Method toExecute = findMethodByName(methodName, m.getDeclaringClass());
			toExecute.invoke(testObject);
		} catch (NullPointerException npe) {
			LOG.error("NPE thrown trying to invoke '{}.{}' The method does not exist or is private", m.getDeclaringClass().getName(), methodName );
		}
	}
	
	
	
}
