begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.rest.section
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|rest
operator|.
name|section
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
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
comment|/**  * Holds a REST test suite loaded from a specific yaml file.  * Supports a setup section and multiple test sections.  */
end_comment

begin_class
DECL|class|RestTestSuite
specifier|public
class|class
name|RestTestSuite
block|{
DECL|field|api
specifier|private
specifier|final
name|String
name|api
decl_stmt|;
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|setupSection
specifier|private
name|SetupSection
name|setupSection
decl_stmt|;
DECL|field|testSections
specifier|private
name|Set
argument_list|<
name|TestSection
argument_list|>
name|testSections
init|=
name|Sets
operator|.
name|newTreeSet
argument_list|()
decl_stmt|;
DECL|method|RestTestSuite
specifier|public
name|RestTestSuite
parameter_list|(
name|String
name|api
parameter_list|,
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|api
operator|=
name|api
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
DECL|method|getApi
specifier|public
name|String
name|getApi
parameter_list|()
block|{
return|return
name|api
return|;
block|}
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
DECL|method|getPath
specifier|public
name|String
name|getPath
parameter_list|()
block|{
return|return
name|api
operator|+
literal|"/"
operator|+
name|name
return|;
block|}
DECL|method|getSetupSection
specifier|public
name|SetupSection
name|getSetupSection
parameter_list|()
block|{
return|return
name|setupSection
return|;
block|}
DECL|method|setSetupSection
specifier|public
name|void
name|setSetupSection
parameter_list|(
name|SetupSection
name|setupSection
parameter_list|)
block|{
name|this
operator|.
name|setupSection
operator|=
name|setupSection
expr_stmt|;
block|}
comment|/**      * Adds a {@link org.elasticsearch.test.rest.section.TestSection} to the REST suite      * @return true if the test section was not already present, false otherwise      */
DECL|method|addTestSection
specifier|public
name|boolean
name|addTestSection
parameter_list|(
name|TestSection
name|testSection
parameter_list|)
block|{
return|return
name|this
operator|.
name|testSections
operator|.
name|add
argument_list|(
name|testSection
argument_list|)
return|;
block|}
DECL|method|getTestSections
specifier|public
name|List
argument_list|<
name|TestSection
argument_list|>
name|getTestSections
parameter_list|()
block|{
return|return
name|Lists
operator|.
name|newArrayList
argument_list|(
name|testSections
argument_list|)
return|;
block|}
block|}
end_class

end_unit
