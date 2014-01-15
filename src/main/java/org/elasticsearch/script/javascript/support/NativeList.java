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
name|Undefined
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
name|List
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|NativeList
specifier|public
class|class
name|NativeList
implements|implements
name|Scriptable
implements|,
name|Wrapper
block|{
DECL|field|serialVersionUID
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|3664761893203964569L
decl_stmt|;
DECL|field|list
specifier|private
name|List
argument_list|<
name|Object
argument_list|>
name|list
decl_stmt|;
DECL|field|parentScope
specifier|private
name|Scriptable
name|parentScope
decl_stmt|;
DECL|field|prototype
specifier|private
name|Scriptable
name|prototype
decl_stmt|;
DECL|method|wrap
specifier|public
specifier|static
name|NativeList
name|wrap
parameter_list|(
name|Scriptable
name|scope
parameter_list|,
name|List
argument_list|<
name|Object
argument_list|>
name|list
parameter_list|)
block|{
return|return
operator|new
name|NativeList
argument_list|(
name|scope
argument_list|,
name|list
argument_list|)
return|;
block|}
DECL|method|NativeList
specifier|public
name|NativeList
parameter_list|(
name|Scriptable
name|scope
parameter_list|,
name|List
argument_list|<
name|Object
argument_list|>
name|list
parameter_list|)
block|{
name|this
operator|.
name|parentScope
operator|=
name|scope
expr_stmt|;
name|this
operator|.
name|list
operator|=
name|list
expr_stmt|;
block|}
comment|/* (non-Javadoc)      * @see org.mozilla.javascript.Wrapper#unwrap()      */
DECL|method|unwrap
specifier|public
name|Object
name|unwrap
parameter_list|()
block|{
return|return
name|list
return|;
block|}
comment|/* (non-Javadoc)      * @see org.mozilla.javascript.Scriptable#getClassName()      */
DECL|method|getClassName
specifier|public
name|String
name|getClassName
parameter_list|()
block|{
return|return
literal|"NativeList"
return|;
block|}
comment|/* (non-Javadoc)      * @see org.mozilla.javascript.Scriptable#get(java.lang.String, org.mozilla.javascript.Scriptable)      */
DECL|method|get
specifier|public
name|Object
name|get
parameter_list|(
name|String
name|name
parameter_list|,
name|Scriptable
name|start
parameter_list|)
block|{
if|if
condition|(
literal|"length"
operator|.
name|equals
argument_list|(
name|name
argument_list|)
condition|)
block|{
return|return
name|list
operator|.
name|size
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|Undefined
operator|.
name|instance
return|;
block|}
block|}
comment|/* (non-Javadoc)      * @see org.mozilla.javascript.Scriptable#get(int, org.mozilla.javascript.Scriptable)      */
DECL|method|get
specifier|public
name|Object
name|get
parameter_list|(
name|int
name|index
parameter_list|,
name|Scriptable
name|start
parameter_list|)
block|{
if|if
condition|(
name|index
operator|<
literal|0
operator|||
name|index
operator|>=
name|list
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
name|Undefined
operator|.
name|instance
return|;
block|}
return|return
name|list
operator|.
name|get
argument_list|(
name|index
argument_list|)
return|;
block|}
comment|/* (non-Javadoc)      * @see org.mozilla.javascript.Scriptable#has(java.lang.String, org.mozilla.javascript.Scriptable)      */
DECL|method|has
specifier|public
name|boolean
name|has
parameter_list|(
name|String
name|name
parameter_list|,
name|Scriptable
name|start
parameter_list|)
block|{
if|if
condition|(
literal|"length"
operator|.
name|equals
argument_list|(
name|name
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/* (non-Javadoc)      * @see org.mozilla.javascript.Scriptable#has(int, org.mozilla.javascript.Scriptable)      */
DECL|method|has
specifier|public
name|boolean
name|has
parameter_list|(
name|int
name|index
parameter_list|,
name|Scriptable
name|start
parameter_list|)
block|{
return|return
name|index
operator|>=
literal|0
operator|&&
name|index
operator|<
name|list
operator|.
name|size
argument_list|()
return|;
block|}
comment|/* (non-Javadoc)      * @see org.mozilla.javascript.Scriptable#put(java.lang.String, org.mozilla.javascript.Scriptable, java.lang.Object)      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|put
specifier|public
name|void
name|put
parameter_list|(
name|String
name|name
parameter_list|,
name|Scriptable
name|start
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
comment|// do nothing here...
block|}
comment|/* (non-Javadoc)      * @see org.mozilla.javascript.Scriptable#put(int, org.mozilla.javascript.Scriptable, java.lang.Object)      */
DECL|method|put
specifier|public
name|void
name|put
parameter_list|(
name|int
name|index
parameter_list|,
name|Scriptable
name|start
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
name|index
operator|==
name|list
operator|.
name|size
argument_list|()
condition|)
block|{
name|list
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|list
operator|.
name|set
argument_list|(
name|index
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
comment|/* (non-Javadoc)      * @see org.mozilla.javascript.Scriptable#delete(java.lang.String)      */
DECL|method|delete
specifier|public
name|void
name|delete
parameter_list|(
name|String
name|name
parameter_list|)
block|{
comment|// nothing here
block|}
comment|/* (non-Javadoc)      * @see org.mozilla.javascript.Scriptable#delete(int)      */
DECL|method|delete
specifier|public
name|void
name|delete
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|list
operator|.
name|remove
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
comment|/* (non-Javadoc)      * @see org.mozilla.javascript.Scriptable#getPrototype()      */
DECL|method|getPrototype
specifier|public
name|Scriptable
name|getPrototype
parameter_list|()
block|{
return|return
name|this
operator|.
name|prototype
return|;
block|}
comment|/* (non-Javadoc)      * @see org.mozilla.javascript.Scriptable#setPrototype(org.mozilla.javascript.Scriptable)      */
DECL|method|setPrototype
specifier|public
name|void
name|setPrototype
parameter_list|(
name|Scriptable
name|prototype
parameter_list|)
block|{
name|this
operator|.
name|prototype
operator|=
name|prototype
expr_stmt|;
block|}
comment|/* (non-Javadoc)      * @see org.mozilla.javascript.Scriptable#getParentScope()      */
DECL|method|getParentScope
specifier|public
name|Scriptable
name|getParentScope
parameter_list|()
block|{
return|return
name|this
operator|.
name|parentScope
return|;
block|}
comment|/* (non-Javadoc)      * @see org.mozilla.javascript.Scriptable#setParentScope(org.mozilla.javascript.Scriptable)      */
DECL|method|setParentScope
specifier|public
name|void
name|setParentScope
parameter_list|(
name|Scriptable
name|parent
parameter_list|)
block|{
name|this
operator|.
name|parentScope
operator|=
name|parent
expr_stmt|;
block|}
comment|/* (non-Javadoc)      * @see org.mozilla.javascript.Scriptable#getIds()      */
DECL|method|getIds
specifier|public
name|Object
index|[]
name|getIds
parameter_list|()
block|{
name|int
name|size
init|=
name|list
operator|.
name|size
argument_list|()
decl_stmt|;
name|Object
index|[]
name|ids
init|=
operator|new
name|Object
index|[
name|size
index|]
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
name|size
condition|;
operator|++
name|i
control|)
block|{
name|ids
index|[
name|i
index|]
operator|=
name|i
expr_stmt|;
block|}
return|return
name|ids
return|;
block|}
comment|/* (non-Javadoc)      * @see org.mozilla.javascript.Scriptable#getDefaultValue(java.lang.Class)      */
DECL|method|getDefaultValue
specifier|public
name|Object
name|getDefaultValue
parameter_list|(
name|Class
name|hint
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
comment|/* (non-Javadoc)      * @see org.mozilla.javascript.Scriptable#hasInstance(org.mozilla.javascript.Scriptable)      */
DECL|method|hasInstance
specifier|public
name|boolean
name|hasInstance
parameter_list|(
name|Scriptable
name|value
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|value
operator|instanceof
name|Wrapper
operator|)
condition|)
return|return
literal|false
return|;
name|Object
name|instance
init|=
operator|(
operator|(
name|Wrapper
operator|)
name|value
operator|)
operator|.
name|unwrap
argument_list|()
decl_stmt|;
return|return
name|List
operator|.
name|class
operator|.
name|isInstance
argument_list|(
name|instance
argument_list|)
return|;
block|}
block|}
end_class

end_unit

