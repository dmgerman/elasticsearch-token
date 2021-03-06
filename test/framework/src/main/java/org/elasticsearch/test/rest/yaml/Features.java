begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.rest.yaml
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|rest
operator|.
name|yaml
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESIntegTestCase
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|unmodifiableList
import|;
end_import

begin_comment
comment|/**  * Allows to register additional features supported by the tests runner.  * This way any runner can add extra features and use proper skip sections to avoid  * breaking others runners till they have implemented the new feature as well.  *  * Once all runners have implemented the feature, it can be removed from the list  * and the related skip sections can be removed from the tests as well.  */
end_comment

begin_class
DECL|class|Features
specifier|public
specifier|final
class|class
name|Features
block|{
DECL|field|SUPPORTED
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|SUPPORTED
init|=
name|unmodifiableList
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"catch_unauthorized"
argument_list|,
literal|"embedded_stash_key"
argument_list|,
literal|"headers"
argument_list|,
literal|"stash_in_key"
argument_list|,
literal|"stash_in_path"
argument_list|,
literal|"stash_path_replace"
argument_list|,
literal|"warnings"
argument_list|,
literal|"yaml"
argument_list|)
argument_list|)
decl_stmt|;
DECL|method|Features
specifier|private
name|Features
parameter_list|()
block|{      }
comment|/**      * Tells whether all the features provided as argument are supported      */
DECL|method|areAllSupported
specifier|public
specifier|static
name|boolean
name|areAllSupported
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|features
parameter_list|)
block|{
for|for
control|(
name|String
name|feature
range|:
name|features
control|)
block|{
if|if
condition|(
literal|"requires_replica"
operator|.
name|equals
argument_list|(
name|feature
argument_list|)
operator|&&
name|ESIntegTestCase
operator|.
name|cluster
argument_list|()
operator|.
name|numDataNodes
argument_list|()
operator|>=
literal|2
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
operator|!
name|SUPPORTED
operator|.
name|contains
argument_list|(
name|feature
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

