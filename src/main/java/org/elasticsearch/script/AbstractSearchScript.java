begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.script
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|script
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
name|AtomicReader
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
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|lookup
operator|.
name|DocLookup
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
name|FieldsLookup
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
name|SearchLookup
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
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * A base class for any script type that is used during the search process (custom score, facets, and so on).  *<p/>  *<p>If the script returns a specific numeric type, consider overriding the type specific base classes  * such as {@link AbstractDoubleSearchScript}, {@link AbstractFloatSearchScript} and {@link AbstractLongSearchScript}  * for better performance.  *<p/>  *<p>The use is required to implement the {@link #run()} method.  */
end_comment

begin_class
DECL|class|AbstractSearchScript
specifier|public
specifier|abstract
class|class
name|AbstractSearchScript
extends|extends
name|AbstractExecutableScript
implements|implements
name|SearchScript
block|{
DECL|field|lookup
specifier|private
name|SearchLookup
name|lookup
decl_stmt|;
DECL|field|score
specifier|private
name|float
name|score
init|=
name|Float
operator|.
name|NaN
decl_stmt|;
comment|/**      * Returns the current score and only applicable when used as a scoring script in a custom score query!.      * For other cases, use {@link #doc()} and get the score from it.      */
DECL|method|score
specifier|protected
specifier|final
name|float
name|score
parameter_list|()
block|{
return|return
name|score
return|;
block|}
comment|/**      * Returns the doc lookup allowing to access field data (cached) values as well as the current document score      * (where applicable).      */
DECL|method|doc
specifier|protected
specifier|final
name|DocLookup
name|doc
parameter_list|()
block|{
return|return
name|lookup
operator|.
name|doc
argument_list|()
return|;
block|}
comment|/**      * Allows to access the actual source (loaded and parsed).      */
DECL|method|source
specifier|protected
specifier|final
name|SourceLookup
name|source
parameter_list|()
block|{
return|return
name|lookup
operator|.
name|source
argument_list|()
return|;
block|}
comment|/**      * Allows to access the *stored* fields.      */
DECL|method|fields
specifier|protected
specifier|final
name|FieldsLookup
name|fields
parameter_list|()
block|{
return|return
name|lookup
operator|.
name|fields
argument_list|()
return|;
block|}
DECL|method|setLookup
name|void
name|setLookup
parameter_list|(
name|SearchLookup
name|lookup
parameter_list|)
block|{
name|this
operator|.
name|lookup
operator|=
name|lookup
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setScorer
specifier|public
name|void
name|setScorer
parameter_list|(
name|Scorer
name|scorer
parameter_list|)
block|{
name|lookup
operator|.
name|setScorer
argument_list|(
name|scorer
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setNextReader
specifier|public
name|void
name|setNextReader
parameter_list|(
name|AtomicReader
name|reader
parameter_list|)
block|{
name|lookup
operator|.
name|setNextReader
argument_list|(
name|reader
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setNextDocId
specifier|public
name|void
name|setNextDocId
parameter_list|(
name|int
name|doc
parameter_list|)
block|{
name|lookup
operator|.
name|setNextDocId
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setNextSource
specifier|public
name|void
name|setNextSource
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
parameter_list|)
block|{
name|lookup
operator|.
name|source
argument_list|()
operator|.
name|setNextSource
argument_list|(
name|source
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setNextScore
specifier|public
name|void
name|setNextScore
parameter_list|(
name|float
name|score
parameter_list|)
block|{
name|this
operator|.
name|score
operator|=
name|score
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|runAsFloat
specifier|public
name|float
name|runAsFloat
parameter_list|()
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|run
argument_list|()
operator|)
operator|.
name|floatValue
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|runAsLong
specifier|public
name|long
name|runAsLong
parameter_list|()
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|run
argument_list|()
operator|)
operator|.
name|longValue
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|runAsDouble
specifier|public
name|double
name|runAsDouble
parameter_list|()
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|run
argument_list|()
operator|)
operator|.
name|doubleValue
argument_list|()
return|;
block|}
block|}
end_class

end_unit

