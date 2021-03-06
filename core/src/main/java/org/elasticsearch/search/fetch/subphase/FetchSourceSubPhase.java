begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.fetch.subphase
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
operator|.
name|subphase
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|BytesStreamOutput
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
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
operator|.
name|FetchSubPhase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|SearchContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|lookup
operator|.
name|SourceLookup
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

begin_class
DECL|class|FetchSourceSubPhase
specifier|public
specifier|final
class|class
name|FetchSourceSubPhase
implements|implements
name|FetchSubPhase
block|{
annotation|@
name|Override
DECL|method|hitExecute
specifier|public
name|void
name|hitExecute
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|HitContext
name|hitContext
parameter_list|)
block|{
if|if
condition|(
name|context
operator|.
name|sourceRequested
argument_list|()
operator|==
literal|false
condition|)
block|{
return|return;
block|}
name|SourceLookup
name|source
init|=
name|context
operator|.
name|lookup
argument_list|()
operator|.
name|source
argument_list|()
decl_stmt|;
name|FetchSourceContext
name|fetchSourceContext
init|=
name|context
operator|.
name|fetchSourceContext
argument_list|()
decl_stmt|;
assert|assert
name|fetchSourceContext
operator|.
name|fetchSource
argument_list|()
assert|;
if|if
condition|(
name|fetchSourceContext
operator|.
name|includes
argument_list|()
operator|.
name|length
operator|==
literal|0
operator|&&
name|fetchSourceContext
operator|.
name|excludes
argument_list|()
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|hitContext
operator|.
name|hit
argument_list|()
operator|.
name|sourceRef
argument_list|(
name|source
operator|.
name|internalSourceRef
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|source
operator|.
name|internalSourceRef
argument_list|()
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unable to fetch fields from _source field: _source is disabled in the mappings "
operator|+
literal|"for index ["
operator|+
name|context
operator|.
name|indexShard
argument_list|()
operator|.
name|shardId
argument_list|()
operator|.
name|getIndexName
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
specifier|final
name|Object
name|value
init|=
name|source
operator|.
name|filter
argument_list|(
name|fetchSourceContext
argument_list|)
decl_stmt|;
try|try
block|{
specifier|final
name|int
name|initialCapacity
init|=
name|Math
operator|.
name|min
argument_list|(
literal|1024
argument_list|,
name|source
operator|.
name|internalSourceRef
argument_list|()
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
name|BytesStreamOutput
name|streamOutput
init|=
operator|new
name|BytesStreamOutput
argument_list|(
name|initialCapacity
argument_list|)
decl_stmt|;
name|XContentBuilder
name|builder
init|=
operator|new
name|XContentBuilder
argument_list|(
name|source
operator|.
name|sourceContentType
argument_list|()
operator|.
name|xContent
argument_list|()
argument_list|,
name|streamOutput
argument_list|)
decl_stmt|;
name|builder
operator|.
name|value
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|hitContext
operator|.
name|hit
argument_list|()
operator|.
name|sourceRef
argument_list|(
name|builder
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"Error filtering source"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

