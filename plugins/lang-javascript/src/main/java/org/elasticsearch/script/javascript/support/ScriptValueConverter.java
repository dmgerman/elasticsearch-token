begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.script.javascript.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|javascript
operator|.
name|support
package|;
end_package

begin_import
import|import
name|org
operator|.
name|mozilla
operator|.
name|javascript
operator|.
name|Context
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mozilla
operator|.
name|javascript
operator|.
name|IdScriptableObject
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mozilla
operator|.
name|javascript
operator|.
name|NativeArray
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mozilla
operator|.
name|javascript
operator|.
name|ScriptRuntime
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mozilla
operator|.
name|javascript
operator|.
name|Scriptable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mozilla
operator|.
name|javascript
operator|.
name|Wrapper
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Date
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * Value Converter to marshal objects between Java and Javascript.  *  *  */
end_comment

begin_class
DECL|class|ScriptValueConverter
specifier|public
specifier|final
class|class
name|ScriptValueConverter
block|{
DECL|field|TYPE_DATE
specifier|private
specifier|static
specifier|final
name|String
name|TYPE_DATE
init|=
literal|"Date"
decl_stmt|;
comment|/**      * Private constructor - methods are static      */
DECL|method|ScriptValueConverter
specifier|private
name|ScriptValueConverter
parameter_list|()
block|{     }
comment|/**      * Convert an object from a script wrapper value to a serializable value valid outside      * of the Rhino script processor context.      *<p>      * This includes converting JavaScript Array objects to Lists of valid objects.      *      * @param value Value to convert from script wrapper object to external object value.      * @return unwrapped and converted value.      */
DECL|method|unwrapValue
specifier|public
specifier|static
name|Object
name|unwrapValue
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|Wrapper
condition|)
block|{
comment|// unwrap a Java object from a JavaScript wrapper
comment|// recursively call this method to convert the unwrapped value
name|value
operator|=
name|unwrapValue
argument_list|(
operator|(
operator|(
name|Wrapper
operator|)
name|value
operator|)
operator|.
name|unwrap
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|IdScriptableObject
condition|)
block|{
comment|// check for special case Native object wrappers
name|String
name|className
init|=
operator|(
operator|(
name|IdScriptableObject
operator|)
name|value
operator|)
operator|.
name|getClassName
argument_list|()
decl_stmt|;
comment|// check for special case of the String object
if|if
condition|(
literal|"String"
operator|.
name|equals
argument_list|(
name|className
argument_list|)
condition|)
block|{
name|value
operator|=
name|Context
operator|.
name|jsToJava
argument_list|(
name|value
argument_list|,
name|String
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
comment|// check for special case of a Date object
elseif|else
if|if
condition|(
literal|"Date"
operator|.
name|equals
argument_list|(
name|className
argument_list|)
condition|)
block|{
name|value
operator|=
name|Context
operator|.
name|jsToJava
argument_list|(
name|value
argument_list|,
name|Date
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// a scriptable object will probably indicate a multi-value property set
comment|// set using a JavaScript associative Array object
name|Scriptable
name|values
init|=
operator|(
name|Scriptable
operator|)
name|value
decl_stmt|;
name|Object
index|[]
name|propIds
init|=
name|values
operator|.
name|getIds
argument_list|()
decl_stmt|;
comment|// is it a JavaScript associative Array object using Integer indexes?
if|if
condition|(
name|values
operator|instanceof
name|NativeArray
operator|&&
name|isArray
argument_list|(
name|propIds
argument_list|)
condition|)
block|{
comment|// convert JavaScript array of values to a List of Serializable objects
name|List
argument_list|<
name|Object
argument_list|>
name|propValues
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|propIds
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|propIds
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
comment|// work on each key in turn
name|Integer
name|propId
init|=
operator|(
name|Integer
operator|)
name|propIds
index|[
name|i
index|]
decl_stmt|;
comment|// we are only interested in keys that indicate a list of values
if|if
condition|(
name|propId
operator|instanceof
name|Integer
condition|)
block|{
comment|// get the value out for the specified key
name|Object
name|val
init|=
name|values
operator|.
name|get
argument_list|(
name|propId
argument_list|,
name|values
argument_list|)
decl_stmt|;
comment|// recursively call this method to convert the value
name|propValues
operator|.
name|add
argument_list|(
name|unwrapValue
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|value
operator|=
name|propValues
expr_stmt|;
block|}
else|else
block|{
comment|// any other JavaScript object that supports properties - convert to a Map of objects
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|propValues
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|(
name|propIds
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|propIds
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
comment|// work on each key in turn
name|Object
name|propId
init|=
name|propIds
index|[
name|i
index|]
decl_stmt|;
comment|// we are only interested in keys that indicate a list of values
if|if
condition|(
name|propId
operator|instanceof
name|String
condition|)
block|{
comment|// get the value out for the specified key
name|Object
name|val
init|=
name|values
operator|.
name|get
argument_list|(
operator|(
name|String
operator|)
name|propId
argument_list|,
name|values
argument_list|)
decl_stmt|;
comment|// recursively call this method to convert the value
name|propValues
operator|.
name|put
argument_list|(
operator|(
name|String
operator|)
name|propId
argument_list|,
name|unwrapValue
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|value
operator|=
name|propValues
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|Object
index|[]
condition|)
block|{
comment|// convert back a list Object Java values
name|Object
index|[]
name|array
init|=
operator|(
name|Object
index|[]
operator|)
name|value
decl_stmt|;
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|array
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|array
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|list
operator|.
name|add
argument_list|(
name|unwrapValue
argument_list|(
name|array
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|value
operator|=
name|list
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|Map
condition|)
block|{
comment|// ensure each value in the Map is unwrapped (which may have been an unwrapped NativeMap!)
name|Map
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|map
init|=
operator|(
name|Map
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
operator|)
name|value
decl_stmt|;
name|Map
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|copyMap
init|=
operator|new
name|HashMap
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
argument_list|(
name|map
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Object
name|key
range|:
name|map
operator|.
name|keySet
argument_list|()
control|)
block|{
name|copyMap
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|unwrapValue
argument_list|(
name|map
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|value
operator|=
name|copyMap
expr_stmt|;
block|}
return|return
name|value
return|;
block|}
comment|/**      * Convert an object from any repository serialized value to a valid script object.      * This includes converting Collection multi-value properties into JavaScript Array objects.      *      * @param scope Scripting scope      * @param value Property value      * @return Value safe for scripting usage      */
DECL|method|wrapValue
specifier|public
specifier|static
name|Object
name|wrapValue
parameter_list|(
name|Scriptable
name|scope
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
comment|// perform conversions from Java objects to JavaScript scriptable instances
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|Date
condition|)
block|{
comment|// convert Date to JavaScript native Date object
comment|// call the "Date" constructor on the root scope object - passing in the millisecond
comment|// value from the Java date - this will construct a JavaScript Date with the same value
name|Date
name|date
init|=
operator|(
name|Date
operator|)
name|value
decl_stmt|;
name|value
operator|=
name|ScriptRuntime
operator|.
name|newObject
argument_list|(
name|Context
operator|.
name|getCurrentContext
argument_list|()
argument_list|,
name|scope
argument_list|,
name|TYPE_DATE
argument_list|,
operator|new
name|Object
index|[]
block|{
name|date
operator|.
name|getTime
argument_list|()
block|}
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|Collection
condition|)
block|{
comment|// recursively convert each value in the collection
name|Collection
argument_list|<
name|Object
argument_list|>
name|collection
init|=
operator|(
name|Collection
argument_list|<
name|Object
argument_list|>
operator|)
name|value
decl_stmt|;
name|Object
index|[]
name|array
init|=
operator|new
name|Object
index|[
name|collection
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|int
name|index
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Object
name|obj
range|:
name|collection
control|)
block|{
name|array
index|[
name|index
operator|++
index|]
operator|=
name|wrapValue
argument_list|(
name|scope
argument_list|,
name|obj
argument_list|)
expr_stmt|;
block|}
comment|// convert array to a native JavaScript Array
name|value
operator|=
name|Context
operator|.
name|getCurrentContext
argument_list|()
operator|.
name|newArray
argument_list|(
name|scope
argument_list|,
name|array
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|Map
condition|)
block|{
name|value
operator|=
name|NativeMap
operator|.
name|wrap
argument_list|(
name|scope
argument_list|,
operator|(
name|Map
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
comment|// simple numbers, strings and booleans are wrapped automatically by Rhino
return|return
name|value
return|;
block|}
comment|/**      * Look at the id's of a native array and try to determine whether it's actually an Array or a Hashmap      *      * @param ids id's of the native array      * @return boolean  true if it's an array, false otherwise (ie it's a map)      */
DECL|method|isArray
specifier|private
specifier|static
name|boolean
name|isArray
parameter_list|(
specifier|final
name|Object
index|[]
name|ids
parameter_list|)
block|{
name|boolean
name|result
init|=
literal|true
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|ids
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|ids
index|[
name|i
index|]
operator|instanceof
name|Integer
operator|==
literal|false
condition|)
block|{
name|result
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

