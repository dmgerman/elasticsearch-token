begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.profile
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|profile
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
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Scorer
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
comment|/** A collector that profiles how much time is spent calling it. */
end_comment

begin_class
DECL|class|ProfileCollector
specifier|final
class|class
name|ProfileCollector
extends|extends
name|FilterCollector
block|{
DECL|field|time
specifier|private
name|long
name|time
decl_stmt|;
comment|/** Sole constructor. */
DECL|method|ProfileCollector
specifier|public
name|ProfileCollector
parameter_list|(
name|Collector
name|in
parameter_list|)
block|{
name|super
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
comment|/** Return the wrapped collector. */
DECL|method|getDelegate
specifier|public
name|Collector
name|getDelegate
parameter_list|()
block|{
return|return
name|in
return|;
block|}
annotation|@
name|Override
DECL|method|needsScores
specifier|public
name|boolean
name|needsScores
parameter_list|()
block|{
specifier|final
name|long
name|start
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
try|try
block|{
return|return
name|super
operator|.
name|needsScores
argument_list|()
return|;
block|}
finally|finally
block|{
name|time
operator|+=
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|start
argument_list|)
expr_stmt|;
block|}
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
specifier|final
name|long
name|start
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
specifier|final
name|LeafCollector
name|inLeafCollector
decl_stmt|;
try|try
block|{
name|inLeafCollector
operator|=
name|super
operator|.
name|getLeafCollector
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|time
operator|+=
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|start
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|FilterLeafCollector
argument_list|(
name|inLeafCollector
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
specifier|final
name|long
name|start
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
try|try
block|{
name|super
operator|.
name|collect
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|time
operator|+=
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|start
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|setScorer
parameter_list|(
name|Scorer
name|scorer
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|long
name|start
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
try|try
block|{
name|super
operator|.
name|setScorer
argument_list|(
name|scorer
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|time
operator|+=
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|start
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|;
block|}
comment|/** Return the total time spent on this collector. */
DECL|method|getTime
specifier|public
name|long
name|getTime
parameter_list|()
block|{
return|return
name|time
return|;
block|}
block|}
end_class

end_unit

