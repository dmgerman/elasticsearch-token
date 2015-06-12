begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
package|;
end_package

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|Booleans
specifier|public
class|class
name|Booleans
block|{
comment|/**      * Returns<code>false</code> if text is in<tt>false</tt>,<tt>0</tt>,<tt>off</tt>,<tt>no</tt>; else, true      */
DECL|method|parseBoolean
specifier|public
specifier|static
name|boolean
name|parseBoolean
parameter_list|(
name|char
index|[]
name|text
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|boolean
name|defaultValue
parameter_list|)
block|{
comment|// TODO: the leniency here is very dangerous: a simple typo will be misinterpreted and the user won't know.
comment|// We should remove it and cutover to https://github.com/rmuir/booleanparser
if|if
condition|(
name|text
operator|==
literal|null
operator|||
name|length
operator|==
literal|0
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
if|if
condition|(
name|length
operator|==
literal|1
condition|)
block|{
return|return
name|text
index|[
name|offset
index|]
operator|!=
literal|'0'
return|;
block|}
if|if
condition|(
name|length
operator|==
literal|2
condition|)
block|{
return|return
operator|!
operator|(
name|text
index|[
name|offset
index|]
operator|==
literal|'n'
operator|&&
name|text
index|[
name|offset
operator|+
literal|1
index|]
operator|==
literal|'o'
operator|)
return|;
block|}
if|if
condition|(
name|length
operator|==
literal|3
condition|)
block|{
return|return
operator|!
operator|(
name|text
index|[
name|offset
index|]
operator|==
literal|'o'
operator|&&
name|text
index|[
name|offset
operator|+
literal|1
index|]
operator|==
literal|'f'
operator|&&
name|text
index|[
name|offset
operator|+
literal|2
index|]
operator|==
literal|'f'
operator|)
return|;
block|}
if|if
condition|(
name|length
operator|==
literal|5
condition|)
block|{
return|return
operator|!
operator|(
name|text
index|[
name|offset
index|]
operator|==
literal|'f'
operator|&&
name|text
index|[
name|offset
operator|+
literal|1
index|]
operator|==
literal|'a'
operator|&&
name|text
index|[
name|offset
operator|+
literal|2
index|]
operator|==
literal|'l'
operator|&&
name|text
index|[
name|offset
operator|+
literal|3
index|]
operator|==
literal|'s'
operator|&&
name|text
index|[
name|offset
operator|+
literal|4
index|]
operator|==
literal|'e'
operator|)
return|;
block|}
return|return
literal|true
return|;
block|}
comment|/**      * returns true if the a sequence of chars is one of "true","false","on","off","yes","no","0","1"      *      * @param text   sequence to check      * @param offset offset to start      * @param length length to check      */
DECL|method|isBoolean
specifier|public
specifier|static
name|boolean
name|isBoolean
parameter_list|(
name|char
index|[]
name|text
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
if|if
condition|(
name|text
operator|==
literal|null
operator|||
name|length
operator|==
literal|0
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|length
operator|==
literal|1
condition|)
block|{
return|return
name|text
index|[
name|offset
index|]
operator|==
literal|'0'
operator|||
name|text
index|[
name|offset
index|]
operator|==
literal|'1'
return|;
block|}
if|if
condition|(
name|length
operator|==
literal|2
condition|)
block|{
return|return
operator|(
name|text
index|[
name|offset
index|]
operator|==
literal|'n'
operator|&&
name|text
index|[
name|offset
operator|+
literal|1
index|]
operator|==
literal|'o'
operator|)
operator|||
operator|(
name|text
index|[
name|offset
index|]
operator|==
literal|'o'
operator|&&
name|text
index|[
name|offset
operator|+
literal|1
index|]
operator|==
literal|'n'
operator|)
return|;
block|}
if|if
condition|(
name|length
operator|==
literal|3
condition|)
block|{
return|return
operator|(
name|text
index|[
name|offset
index|]
operator|==
literal|'o'
operator|&&
name|text
index|[
name|offset
operator|+
literal|1
index|]
operator|==
literal|'f'
operator|&&
name|text
index|[
name|offset
operator|+
literal|2
index|]
operator|==
literal|'f'
operator|)
operator|||
operator|(
name|text
index|[
name|offset
index|]
operator|==
literal|'y'
operator|&&
name|text
index|[
name|offset
operator|+
literal|1
index|]
operator|==
literal|'e'
operator|&&
name|text
index|[
name|offset
operator|+
literal|2
index|]
operator|==
literal|'s'
operator|)
return|;
block|}
if|if
condition|(
name|length
operator|==
literal|4
condition|)
block|{
return|return
operator|(
name|text
index|[
name|offset
index|]
operator|==
literal|'t'
operator|&&
name|text
index|[
name|offset
operator|+
literal|1
index|]
operator|==
literal|'r'
operator|&&
name|text
index|[
name|offset
operator|+
literal|2
index|]
operator|==
literal|'u'
operator|&&
name|text
index|[
name|offset
operator|+
literal|3
index|]
operator|==
literal|'e'
operator|)
return|;
block|}
if|if
condition|(
name|length
operator|==
literal|5
condition|)
block|{
return|return
operator|(
name|text
index|[
name|offset
index|]
operator|==
literal|'f'
operator|&&
name|text
index|[
name|offset
operator|+
literal|1
index|]
operator|==
literal|'a'
operator|&&
name|text
index|[
name|offset
operator|+
literal|2
index|]
operator|==
literal|'l'
operator|&&
name|text
index|[
name|offset
operator|+
literal|3
index|]
operator|==
literal|'s'
operator|&&
name|text
index|[
name|offset
operator|+
literal|4
index|]
operator|==
literal|'e'
operator|)
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/***      *      * @param value      * @return true/false      * throws exception if string cannot be parsed to boolean      */
DECL|method|parseBooleanExact
specifier|public
specifier|static
name|Boolean
name|parseBooleanExact
parameter_list|(
name|String
name|value
parameter_list|)
block|{
name|boolean
name|isFalse
init|=
name|isExplicitFalse
argument_list|(
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|isFalse
condition|)
block|{
return|return
literal|false
return|;
block|}
name|boolean
name|isTrue
init|=
name|isExplicitTrue
argument_list|(
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|isTrue
condition|)
block|{
return|return
literal|true
return|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"value cannot be parsed to boolean [ true/1/on/yes OR false/0/off/no ]  "
argument_list|)
throw|;
block|}
DECL|method|parseBoolean
specifier|public
specifier|static
name|Boolean
name|parseBoolean
parameter_list|(
name|String
name|value
parameter_list|,
name|Boolean
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
comment|// only for the null case we do that here!
return|return
name|defaultValue
return|;
block|}
return|return
name|parseBoolean
argument_list|(
name|value
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**      * Returns<code>true</code> iff the value is neither of the following:      *<tt>false</tt>,<tt>0</tt>,<tt>off</tt>,<tt>no</tt>      *   otherwise<code>false</code>      */
DECL|method|parseBoolean
specifier|public
specifier|static
name|boolean
name|parseBoolean
parameter_list|(
name|String
name|value
parameter_list|,
name|boolean
name|defaultValue
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
name|defaultValue
return|;
block|}
return|return
operator|!
operator|(
name|value
operator|.
name|equals
argument_list|(
literal|"false"
argument_list|)
operator|||
name|value
operator|.
name|equals
argument_list|(
literal|"0"
argument_list|)
operator|||
name|value
operator|.
name|equals
argument_list|(
literal|"off"
argument_list|)
operator|||
name|value
operator|.
name|equals
argument_list|(
literal|"no"
argument_list|)
operator|)
return|;
block|}
comment|/**      * Returns<code>true</code> iff the value is either of the following:      *<tt>false</tt>,<tt>0</tt>,<tt>off</tt>,<tt>no</tt>      *   otherwise<code>false</code>      */
DECL|method|isExplicitFalse
specifier|public
specifier|static
name|boolean
name|isExplicitFalse
parameter_list|(
name|String
name|value
parameter_list|)
block|{
return|return
name|value
operator|!=
literal|null
operator|&&
operator|(
name|value
operator|.
name|equals
argument_list|(
literal|"false"
argument_list|)
operator|||
name|value
operator|.
name|equals
argument_list|(
literal|"0"
argument_list|)
operator|||
name|value
operator|.
name|equals
argument_list|(
literal|"off"
argument_list|)
operator|||
name|value
operator|.
name|equals
argument_list|(
literal|"no"
argument_list|)
operator|)
return|;
block|}
comment|/**      * Returns<code>true</code> iff the value is either of the following:      *<tt>true</tt>,<tt>1</tt>,<tt>on</tt>,<tt>yes</tt>      *   otherwise<code>false</code>      */
DECL|method|isExplicitTrue
specifier|public
specifier|static
name|boolean
name|isExplicitTrue
parameter_list|(
name|String
name|value
parameter_list|)
block|{
return|return
name|value
operator|!=
literal|null
operator|&&
operator|(
name|value
operator|.
name|equals
argument_list|(
literal|"true"
argument_list|)
operator|||
name|value
operator|.
name|equals
argument_list|(
literal|"1"
argument_list|)
operator|||
name|value
operator|.
name|equals
argument_list|(
literal|"on"
argument_list|)
operator|||
name|value
operator|.
name|equals
argument_list|(
literal|"yes"
argument_list|)
operator|)
return|;
block|}
block|}
end_class

end_unit
