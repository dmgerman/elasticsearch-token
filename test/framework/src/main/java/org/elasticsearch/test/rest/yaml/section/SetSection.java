begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.rest.yaml.section
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
operator|.
name|section
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
name|xcontent
operator|.
name|XContentLocation
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
name|rest
operator|.
name|yaml
operator|.
name|ClientYamlTestExecutionContext
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|Map
import|;
end_import

begin_comment
comment|/**  * Represents a set section:  *  *   - set: {_scroll_id: scroll_id}  *  */
end_comment

begin_class
DECL|class|SetSection
specifier|public
class|class
name|SetSection
implements|implements
name|ExecutableSection
block|{
DECL|field|stash
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|stash
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|location
specifier|private
specifier|final
name|XContentLocation
name|location
decl_stmt|;
DECL|method|SetSection
specifier|public
name|SetSection
parameter_list|(
name|XContentLocation
name|location
parameter_list|)
block|{
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
block|}
DECL|method|addSet
specifier|public
name|void
name|addSet
parameter_list|(
name|String
name|responseField
parameter_list|,
name|String
name|stashedField
parameter_list|)
block|{
name|stash
operator|.
name|put
argument_list|(
name|responseField
argument_list|,
name|stashedField
argument_list|)
expr_stmt|;
block|}
DECL|method|getStash
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getStash
parameter_list|()
block|{
return|return
name|stash
return|;
block|}
annotation|@
name|Override
DECL|method|getLocation
specifier|public
name|XContentLocation
name|getLocation
parameter_list|()
block|{
return|return
name|location
return|;
block|}
annotation|@
name|Override
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|(
name|ClientYamlTestExecutionContext
name|executionContext
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|stash
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Object
name|actualValue
init|=
name|executionContext
operator|.
name|response
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|executionContext
operator|.
name|stash
argument_list|()
operator|.
name|stashValue
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|actualValue
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit
