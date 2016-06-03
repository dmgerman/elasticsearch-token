begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest.core
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|core
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
name|io
operator|.
name|stream
operator|.
name|Writeable
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
name|ToXContent
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
name|XContentBuilder
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
import|;
end_import

begin_class
DECL|class|IngestInfo
specifier|public
class|class
name|IngestInfo
implements|implements
name|Writeable
implements|,
name|ToXContent
block|{
DECL|field|processors
specifier|private
specifier|final
name|Set
argument_list|<
name|ProcessorInfo
argument_list|>
name|processors
decl_stmt|;
DECL|method|IngestInfo
specifier|public
name|IngestInfo
parameter_list|(
name|List
argument_list|<
name|ProcessorInfo
argument_list|>
name|processors
parameter_list|)
block|{
name|this
operator|.
name|processors
operator|=
operator|new
name|TreeSet
argument_list|<>
argument_list|(
name|processors
argument_list|)
expr_stmt|;
comment|// we use a treeset here to have a test-able / predictable order
block|}
comment|/**      * Read from a stream.      */
DECL|method|IngestInfo
specifier|public
name|IngestInfo
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|processors
operator|=
operator|new
name|TreeSet
argument_list|<>
argument_list|()
expr_stmt|;
specifier|final
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
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
name|i
operator|++
control|)
block|{
name|processors
operator|.
name|add
argument_list|(
operator|new
name|ProcessorInfo
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|write
argument_list|(
name|processors
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|ProcessorInfo
name|info
range|:
name|processors
control|)
block|{
name|info
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|getProcessors
specifier|public
name|Iterable
argument_list|<
name|ProcessorInfo
argument_list|>
name|getProcessors
parameter_list|()
block|{
return|return
name|processors
return|;
block|}
DECL|method|containsProcessor
specifier|public
name|boolean
name|containsProcessor
parameter_list|(
name|String
name|type
parameter_list|)
block|{
return|return
name|processors
operator|.
name|contains
argument_list|(
operator|new
name|ProcessorInfo
argument_list|(
name|type
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|(
literal|"ingest"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
literal|"processors"
argument_list|)
expr_stmt|;
for|for
control|(
name|ProcessorInfo
name|info
range|:
name|processors
control|)
block|{
name|info
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|IngestInfo
name|that
init|=
operator|(
name|IngestInfo
operator|)
name|o
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|processors
argument_list|,
name|that
operator|.
name|processors
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|processors
argument_list|)
return|;
block|}
block|}
end_class

end_unit

