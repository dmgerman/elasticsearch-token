begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|query
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|LeafReaderContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|CollectionTerminatedException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Collector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|FilterCollector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|FilterLeafCollector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|LeafCollector
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_comment
comment|/**  * A {@link Collector} that early terminates collection after<code>maxCountHits</code> docs have been collected.  */
end_comment

begin_class
DECL|class|EarlyTerminatingCollector
specifier|public
class|class
name|EarlyTerminatingCollector
extends|extends
name|FilterCollector
block|{
DECL|field|maxCountHits
specifier|private
specifier|final
name|int
name|maxCountHits
decl_stmt|;
DECL|field|numCollected
specifier|private
name|int
name|numCollected
decl_stmt|;
DECL|field|terminatedEarly
specifier|private
name|boolean
name|terminatedEarly
init|=
literal|false
decl_stmt|;
DECL|method|EarlyTerminatingCollector
name|EarlyTerminatingCollector
parameter_list|(
specifier|final
name|Collector
name|delegate
parameter_list|,
name|int
name|maxCountHits
parameter_list|)
block|{
name|super
argument_list|(
name|delegate
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxCountHits
operator|=
name|maxCountHits
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getLeafCollector
specifier|public
name|LeafCollector
name|getLeafCollector
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|numCollected
operator|>=
name|maxCountHits
condition|)
block|{
throw|throw
operator|new
name|CollectionTerminatedException
argument_list|()
throw|;
block|}
return|return
operator|new
name|FilterLeafCollector
argument_list|(
name|super
operator|.
name|getLeafCollector
argument_list|(
name|context
argument_list|)
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|collect
parameter_list|(
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|collect
argument_list|(
name|doc
argument_list|)
expr_stmt|;
if|if
condition|(
operator|++
name|numCollected
operator|>=
name|maxCountHits
condition|)
block|{
name|terminatedEarly
operator|=
literal|true
expr_stmt|;
throw|throw
operator|new
name|CollectionTerminatedException
argument_list|()
throw|;
block|}
block|}
empty_stmt|;
block|}
return|;
block|}
DECL|method|terminatedEarly
specifier|public
name|boolean
name|terminatedEarly
parameter_list|()
block|{
return|return
name|terminatedEarly
return|;
block|}
block|}
end_class

end_unit
