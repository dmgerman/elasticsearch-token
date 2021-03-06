begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.rollover
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|rollover
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|unit
operator|.
name|TimeValue
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

begin_comment
comment|/**  * Condition for index maximum age. Evaluates to<code>true</code>  * when the index is at least {@link #value} old  */
end_comment

begin_class
DECL|class|MaxAgeCondition
specifier|public
class|class
name|MaxAgeCondition
extends|extends
name|Condition
argument_list|<
name|TimeValue
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"max_age"
decl_stmt|;
DECL|method|MaxAgeCondition
specifier|public
name|MaxAgeCondition
parameter_list|(
name|TimeValue
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|NAME
argument_list|)
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
DECL|method|MaxAgeCondition
specifier|public
name|MaxAgeCondition
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|NAME
argument_list|)
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
name|in
operator|.
name|readLong
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|evaluate
specifier|public
name|Result
name|evaluate
parameter_list|(
specifier|final
name|Stats
name|stats
parameter_list|)
block|{
name|long
name|indexAge
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|stats
operator|.
name|indexCreated
decl_stmt|;
return|return
operator|new
name|Result
argument_list|(
name|this
argument_list|,
name|this
operator|.
name|value
operator|.
name|getMillis
argument_list|()
operator|<=
name|indexAge
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getWriteableName
specifier|public
name|String
name|getWriteableName
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeLong
argument_list|(
name|value
operator|.
name|getMillis
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

