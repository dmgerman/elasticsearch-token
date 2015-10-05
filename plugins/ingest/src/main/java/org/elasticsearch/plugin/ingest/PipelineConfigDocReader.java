begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugin.ingest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|ingest
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|SearchResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|IndicesOptions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Client
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
name|component
operator|.
name|AbstractLifecycleComponent
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
name|Inject
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
name|Injector
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
name|settings
operator|.
name|Settings
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
name|search
operator|.
name|SearchHit
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
name|sort
operator|.
name|SortOrder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_class
DECL|class|PipelineConfigDocReader
specifier|public
class|class
name|PipelineConfigDocReader
extends|extends
name|AbstractLifecycleComponent
block|{
DECL|field|client
specifier|private
specifier|volatile
name|Client
name|client
decl_stmt|;
DECL|field|injector
specifier|private
specifier|final
name|Injector
name|injector
decl_stmt|;
DECL|field|scrollTimeout
specifier|private
specifier|final
name|TimeValue
name|scrollTimeout
decl_stmt|;
annotation|@
name|Inject
DECL|method|PipelineConfigDocReader
specifier|public
name|PipelineConfigDocReader
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Injector
name|injector
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|injector
operator|=
name|injector
expr_stmt|;
name|this
operator|.
name|scrollTimeout
operator|=
name|settings
operator|.
name|getAsTime
argument_list|(
literal|"ingest.pipeline.store.scroll.timeout"
argument_list|,
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|30
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doStart
specifier|protected
name|void
name|doStart
parameter_list|()
block|{
name|client
operator|=
name|injector
operator|.
name|getInstance
argument_list|(
name|Client
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doStop
specifier|protected
name|void
name|doStop
parameter_list|()
block|{
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doClose
specifier|protected
name|void
name|doClose
parameter_list|()
block|{     }
DECL|method|readAll
specifier|public
name|Iterable
argument_list|<
name|SearchHit
argument_list|>
name|readAll
parameter_list|()
block|{
comment|// TODO: the search should be replaced with an ingest API when it is available
name|SearchResponse
name|searchResponse
init|=
name|client
operator|.
name|prepareSearch
argument_list|(
name|PipelineStore
operator|.
name|INDEX
argument_list|)
operator|.
name|setVersion
argument_list|(
literal|true
argument_list|)
operator|.
name|setScroll
argument_list|(
name|scrollTimeout
argument_list|)
operator|.
name|addSort
argument_list|(
literal|"_doc"
argument_list|,
name|SortOrder
operator|.
name|ASC
argument_list|)
operator|.
name|setIndicesOptions
argument_list|(
name|IndicesOptions
operator|.
name|lenientExpandOpen
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
name|logger
operator|.
name|debug
argument_list|(
literal|"reading [{}] pipeline documents"
argument_list|,
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|Iterable
argument_list|<
name|SearchHit
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|SearchHit
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
operator|new
name|SearchScrollIterator
argument_list|(
name|searchResponse
argument_list|)
return|;
block|}
block|}
return|;
block|}
DECL|class|SearchScrollIterator
class|class
name|SearchScrollIterator
implements|implements
name|Iterator
argument_list|<
name|SearchHit
argument_list|>
block|{
DECL|field|searchResponse
specifier|private
name|SearchResponse
name|searchResponse
decl_stmt|;
DECL|field|currentIndex
specifier|private
name|int
name|currentIndex
decl_stmt|;
DECL|field|currentHits
specifier|private
name|SearchHit
index|[]
name|currentHits
decl_stmt|;
DECL|method|SearchScrollIterator
name|SearchScrollIterator
parameter_list|(
name|SearchResponse
name|searchResponse
parameter_list|)
block|{
name|this
operator|.
name|searchResponse
operator|=
name|searchResponse
expr_stmt|;
name|this
operator|.
name|currentHits
operator|=
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|hasNext
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
if|if
condition|(
name|currentIndex
operator|<
name|currentHits
operator|.
name|length
condition|)
block|{
return|return
literal|true
return|;
block|}
else|else
block|{
if|if
condition|(
name|searchResponse
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|searchResponse
operator|=
name|client
operator|.
name|prepareSearchScroll
argument_list|(
name|searchResponse
operator|.
name|getScrollId
argument_list|()
argument_list|)
operator|.
name|setScroll
argument_list|(
name|scrollTimeout
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
if|if
condition|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|searchResponse
operator|=
literal|null
expr_stmt|;
return|return
literal|false
return|;
block|}
else|else
block|{
name|currentHits
operator|=
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
expr_stmt|;
name|currentIndex
operator|=
literal|0
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|next
specifier|public
name|SearchHit
name|next
parameter_list|()
block|{
name|SearchHit
name|hit
init|=
name|currentHits
index|[
name|currentIndex
operator|++
index|]
decl_stmt|;
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"reading pipeline document [{}] with source [{}]"
argument_list|,
name|hit
operator|.
name|getId
argument_list|()
argument_list|,
name|hit
operator|.
name|sourceAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|hit
return|;
block|}
block|}
block|}
end_class

end_unit

