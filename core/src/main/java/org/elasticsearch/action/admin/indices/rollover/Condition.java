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
name|ParseField
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
name|ParseFieldMatcherSupplier
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
name|NamedWriteable
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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|ObjectParser
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
name|Set
import|;
end_import

begin_class
DECL|class|Condition
specifier|public
specifier|abstract
class|class
name|Condition
parameter_list|<
name|T
parameter_list|>
implements|implements
name|NamedWriteable
block|{
DECL|field|PARSER
specifier|public
specifier|static
name|ObjectParser
argument_list|<
name|Set
argument_list|<
name|Condition
argument_list|>
argument_list|,
name|ParseFieldMatcherSupplier
argument_list|>
name|PARSER
init|=
operator|new
name|ObjectParser
argument_list|<>
argument_list|(
literal|"conditions"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
static|static
block|{
name|PARSER
operator|.
name|declareString
argument_list|(
parameter_list|(
name|conditions
parameter_list|,
name|s
parameter_list|)
lambda|->
name|conditions
operator|.
name|add
argument_list|(
operator|new
name|MaxAge
argument_list|(
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|s
argument_list|,
name|MaxAge
operator|.
name|NAME
argument_list|)
argument_list|)
argument_list|)
argument_list|,
operator|new
name|ParseField
argument_list|(
name|MaxAge
operator|.
name|NAME
argument_list|)
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareLong
argument_list|(
parameter_list|(
name|conditions
parameter_list|,
name|value
parameter_list|)
lambda|->
name|conditions
operator|.
name|add
argument_list|(
operator|new
name|MaxDocs
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|,
operator|new
name|ParseField
argument_list|(
name|MaxDocs
operator|.
name|NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|class|MaxAge
specifier|public
specifier|static
class|class
name|MaxAge
extends|extends
name|Condition
argument_list|<
name|TimeValue
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|final
specifier|static
name|String
name|NAME
init|=
literal|"max_age"
decl_stmt|;
DECL|method|MaxAge
specifier|public
name|MaxAge
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
DECL|method|MaxAge
specifier|public
name|MaxAge
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
DECL|method|matches
specifier|public
name|boolean
name|matches
parameter_list|(
name|TimeValue
name|value
parameter_list|)
block|{
return|return
name|this
operator|.
name|value
operator|.
name|getMillis
argument_list|()
operator|<=
name|value
operator|.
name|getMillis
argument_list|()
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
DECL|class|MaxDocs
specifier|public
specifier|static
class|class
name|MaxDocs
extends|extends
name|Condition
argument_list|<
name|Long
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|final
specifier|static
name|String
name|NAME
init|=
literal|"max_docs"
decl_stmt|;
DECL|method|MaxDocs
specifier|public
name|MaxDocs
parameter_list|(
name|Long
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
DECL|method|MaxDocs
specifier|public
name|MaxDocs
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
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|matches
specifier|public
name|boolean
name|matches
parameter_list|(
name|Long
name|value
parameter_list|)
block|{
return|return
name|this
operator|.
name|value
operator|<=
name|value
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
argument_list|)
expr_stmt|;
block|}
block|}
DECL|field|value
specifier|protected
name|T
name|value
decl_stmt|;
DECL|field|name
specifier|protected
specifier|final
name|String
name|name
decl_stmt|;
DECL|method|Condition
specifier|protected
name|Condition
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
DECL|method|matches
specifier|public
specifier|abstract
name|boolean
name|matches
parameter_list|(
name|T
name|value
parameter_list|)
function_decl|;
annotation|@
name|Override
DECL|method|toString
specifier|public
specifier|final
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"["
operator|+
name|name
operator|+
literal|": "
operator|+
name|value
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit

