begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.inject
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
operator|.
name|spi
operator|.
name|Element
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
operator|.
name|spi
operator|.
name|Elements
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
operator|.
name|spi
operator|.
name|LinkedKeyBinding
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
operator|.
name|spi
operator|.
name|ProviderInstanceBinding
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESTestCase
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|Set
import|;
end_import

begin_comment
comment|/**  * Base testcase for testing {@link Module} implementations.  */
end_comment

begin_class
DECL|class|ModuleTestCase
specifier|public
specifier|abstract
class|class
name|ModuleTestCase
extends|extends
name|ESTestCase
block|{
comment|/** Configures the module and asserts "clazz" is bound to "to". */
DECL|method|assertBinding
specifier|public
name|void
name|assertBinding
parameter_list|(
name|Module
name|module
parameter_list|,
name|Class
name|to
parameter_list|,
name|Class
name|clazz
parameter_list|)
block|{
name|List
argument_list|<
name|Element
argument_list|>
name|elements
init|=
name|Elements
operator|.
name|getElements
argument_list|(
name|module
argument_list|)
decl_stmt|;
for|for
control|(
name|Element
name|element
range|:
name|elements
control|)
block|{
if|if
condition|(
name|element
operator|instanceof
name|LinkedKeyBinding
condition|)
block|{
name|LinkedKeyBinding
name|binding
init|=
operator|(
name|LinkedKeyBinding
operator|)
name|element
decl_stmt|;
if|if
condition|(
name|to
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|binding
operator|.
name|getKey
argument_list|()
operator|.
name|getTypeLiteral
argument_list|()
operator|.
name|getType
argument_list|()
operator|.
name|getTypeName
argument_list|()
argument_list|)
condition|)
block|{
name|assertEquals
argument_list|(
name|clazz
operator|.
name|getName
argument_list|()
argument_list|,
name|binding
operator|.
name|getLinkedKey
argument_list|()
operator|.
name|getTypeLiteral
argument_list|()
operator|.
name|getType
argument_list|()
operator|.
name|getTypeName
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
block|}
name|StringBuilder
name|s
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Element
name|element
range|:
name|elements
control|)
block|{
name|s
operator|.
name|append
argument_list|(
name|element
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
name|fail
argument_list|(
literal|"Did not find any binding to "
operator|+
name|to
operator|.
name|getName
argument_list|()
operator|+
literal|". Found these bindings:\n"
operator|+
name|s
argument_list|)
expr_stmt|;
block|}
comment|/**      * Attempts to configure the module, and asserts an {@link IllegalArgumentException} is      * caught, containing the given messages      */
DECL|method|assertBindingFailure
specifier|public
name|void
name|assertBindingFailure
parameter_list|(
name|Module
name|module
parameter_list|,
name|String
modifier|...
name|msgs
parameter_list|)
block|{
try|try
block|{
name|List
argument_list|<
name|Element
argument_list|>
name|elements
init|=
name|Elements
operator|.
name|getElements
argument_list|(
name|module
argument_list|)
decl_stmt|;
name|StringBuilder
name|s
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Element
name|element
range|:
name|elements
control|)
block|{
name|s
operator|.
name|append
argument_list|(
name|element
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
name|fail
argument_list|(
literal|"Expected exception from configuring module. Found these bindings:\n"
operator|+
name|s
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
for|for
control|(
name|String
name|msg
range|:
name|msgs
control|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
name|msg
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**      * Configures the module and checks a Set of the "to" class      * is bound to "classes". There may be more classes bound      * to "to" than just "classes".      */
DECL|method|assertSetMultiBinding
specifier|public
name|void
name|assertSetMultiBinding
parameter_list|(
name|Module
name|module
parameter_list|,
name|Class
name|to
parameter_list|,
name|Class
modifier|...
name|classes
parameter_list|)
block|{
name|List
argument_list|<
name|Element
argument_list|>
name|elements
init|=
name|Elements
operator|.
name|getElements
argument_list|(
name|module
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|bindings
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|boolean
name|providerFound
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Element
name|element
range|:
name|elements
control|)
block|{
if|if
condition|(
name|element
operator|instanceof
name|LinkedKeyBinding
condition|)
block|{
name|LinkedKeyBinding
name|binding
init|=
operator|(
name|LinkedKeyBinding
operator|)
name|element
decl_stmt|;
if|if
condition|(
name|to
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|binding
operator|.
name|getKey
argument_list|()
operator|.
name|getTypeLiteral
argument_list|()
operator|.
name|getType
argument_list|()
operator|.
name|getTypeName
argument_list|()
argument_list|)
condition|)
block|{
name|bindings
operator|.
name|add
argument_list|(
name|binding
operator|.
name|getLinkedKey
argument_list|()
operator|.
name|getTypeLiteral
argument_list|()
operator|.
name|getType
argument_list|()
operator|.
name|getTypeName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|element
operator|instanceof
name|ProviderInstanceBinding
condition|)
block|{
name|ProviderInstanceBinding
name|binding
init|=
operator|(
name|ProviderInstanceBinding
operator|)
name|element
decl_stmt|;
name|String
name|setType
init|=
name|binding
operator|.
name|getKey
argument_list|()
operator|.
name|getTypeLiteral
argument_list|()
operator|.
name|getType
argument_list|()
operator|.
name|getTypeName
argument_list|()
decl_stmt|;
if|if
condition|(
name|setType
operator|.
name|equals
argument_list|(
literal|"java.util.Set<"
operator|+
name|to
operator|.
name|getName
argument_list|()
operator|+
literal|">"
argument_list|)
condition|)
block|{
name|providerFound
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
for|for
control|(
name|Class
name|clazz
range|:
name|classes
control|)
block|{
if|if
condition|(
name|bindings
operator|.
name|contains
argument_list|(
name|clazz
operator|.
name|getName
argument_list|()
argument_list|)
operator|==
literal|false
condition|)
block|{
name|fail
argument_list|(
literal|"Expected to find "
operator|+
name|clazz
operator|.
name|getName
argument_list|()
operator|+
literal|" as set binding to "
operator|+
name|to
operator|.
name|getName
argument_list|()
operator|+
literal|", found these classes:\n"
operator|+
name|bindings
argument_list|)
expr_stmt|;
block|}
block|}
name|assertTrue
argument_list|(
literal|"Did not find provider for set of "
operator|+
name|to
operator|.
name|getName
argument_list|()
argument_list|,
name|providerFound
argument_list|)
expr_stmt|;
block|}
comment|// TODO: add assert for map multibinding
block|}
end_class

end_unit

