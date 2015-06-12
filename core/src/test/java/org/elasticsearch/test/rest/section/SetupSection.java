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
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  * Represents a setup section. Holds a skip section and multiple do sections.  */
end_comment

begin_class
DECL|class|SetupSection
specifier|public
class|class
name|SetupSection
block|{
DECL|field|EMPTY
specifier|public
specifier|static
specifier|final
name|SetupSection
name|EMPTY
decl_stmt|;
static|static
block|{
name|EMPTY
operator|=
operator|new
name|SetupSection
argument_list|()
expr_stmt|;
name|EMPTY
operator|.
name|setSkipSection
argument_list|(
name|SkipSection
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
block|}
DECL|field|skipSection
specifier|private
name|SkipSection
name|skipSection
decl_stmt|;
DECL|field|doSections
specifier|private
name|List
argument_list|<
name|DoSection
argument_list|>
name|doSections
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
DECL|method|getSkipSection
specifier|public
name|SkipSection
name|getSkipSection
parameter_list|()
block|{
return|return
name|skipSection
return|;
block|}
DECL|method|setSkipSection
specifier|public
name|void
name|setSkipSection
parameter_list|(
name|SkipSection
name|skipSection
parameter_list|)
block|{
name|this
operator|.
name|skipSection
operator|=
name|skipSection
expr_stmt|;
block|}
DECL|method|getDoSections
specifier|public
name|List
argument_list|<
name|DoSection
argument_list|>
name|getDoSections
parameter_list|()
block|{
return|return
name|doSections
return|;
block|}
DECL|method|addDoSection
specifier|public
name|void
name|addDoSection
parameter_list|(
name|DoSection
name|doSection
parameter_list|)
block|{
name|this
operator|.
name|doSections
operator|.
name|add
argument_list|(
name|doSection
argument_list|)
expr_stmt|;
block|}
DECL|method|isEmpty
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|EMPTY
operator|.
name|equals
argument_list|(
name|this
argument_list|)
return|;
block|}
block|}
end_class

end_unit
