package com.centec.webapp.test.reflect;

import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isTransient;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.unitils.reflectionassert.ReflectionComparator;
import org.unitils.reflectionassert.comparator.impl.ObjectComparator;
import org.unitils.reflectionassert.difference.Difference;
import org.unitils.reflectionassert.difference.ObjectDifference;

public class MyObjectComparator extends ObjectComparator {

	
    @Override
    protected void compareFields(Object left, Object right, Class<?> clazz, ObjectDifference difference, boolean onlyFirstDifference, ReflectionComparator reflectionComparator) {
    	 Field[] fields = clazz.getDeclaredFields();
         AccessibleObject.setAccessible(fields, true);
         
         List<String> ignoreFields = new ArrayList<>();
         ignoreFields.add("entityMapper");
         ignoreFields.add("identifier");

         
         for (Field field : fields) {
             // skip transient and static fields
             if (isTransient(field.getModifiers()) || isStatic(field.getModifiers()) || field.isSynthetic()) {
                 continue;
             }
             //skip these fields
             if(ignoreFields.contains(field.getName())){
            	 continue;
             }
             try {
                 // recursively check the value of the fields
                 Difference innerDifference = reflectionComparator.getDifference(field.get(left), field.get(right), onlyFirstDifference);
                 if (innerDifference != null) {
                     difference.addFieldDifference(field.getName(), innerDifference);
                     if (onlyFirstDifference) {
                         return;
                     }
                 }
             } catch (IllegalAccessException e) {
                 // this can't happen. Would get a Security exception instead
                 // throw a runtime exception in case the impossible happens.
                 throw new InternalError("Unexpected IllegalAccessException");
             }
         }

         // compare fields declared in superclass
         Class<?> superclazz = clazz.getSuperclass();
         while (superclazz != null && !superclazz.getName().startsWith("java.lang")) {
             compareFields(left, right, superclazz, difference, onlyFirstDifference, reflectionComparator);
             superclazz = superclazz.getSuperclass();
         }
     }

}
