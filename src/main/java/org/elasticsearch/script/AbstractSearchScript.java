begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|Scorer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|ScriptDocValues
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
name|*
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
name|Map
import|;
end_import

begin_comment
comment|/**  * A base class for any script type that is used during the search process (custom score, aggs, and so on).  *<p/>  *<p>If the script returns a specific numeric type, consider overriding the type specific base classes  * such as {@link AbstractDoubleSearchScript}, {@link AbstractFloatSearchScript} and {@link AbstractLongSearchScript}  * for better performance.  *<p/>  *<p>The use is required to implement the {@link #run()} method.  */
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
DECL|field|scorer
specifier|private
name|Scorer
name|scorer
decl_stmt|;
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
comment|/**      * Returns the current score and only applicable when used as a scoring script in a custom score query!.      */
DECL|method|score
specifier|protected
specifier|final
name|float
name|score
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|scorer
operator|.
name|score
argument_list|()
return|;
block|}
comment|/**      * Returns field data strings access for the provided field.      */
DECL|method|docFieldStrings
specifier|protected
name|ScriptDocValues
operator|.
name|Strings
name|docFieldStrings
parameter_list|(
name|String
name|field
parameter_list|)
block|{
return|return
operator|(
name|ScriptDocValues
operator|.
name|Strings
operator|)
name|doc
argument_list|()
operator|.
name|get
argument_list|(
name|field
argument_list|)
return|;
block|}
comment|/**      * Returns field data double (floating point) access for the provided field.      */
DECL|method|docFieldDoubles
specifier|protected
name|ScriptDocValues
operator|.
name|Doubles
name|docFieldDoubles
parameter_list|(
name|String
name|field
parameter_list|)
block|{
return|return
operator|(
name|ScriptDocValues
operator|.
name|Doubles
operator|)
name|doc
argument_list|()
operator|.
name|get
argument_list|(
name|field
argument_list|)
return|;
block|}
comment|/**      * Returns field data long (integers) access for the provided field.      */
DECL|method|docFieldLongs
specifier|protected
name|ScriptDocValues
operator|.
name|Longs
name|docFieldLongs
parameter_list|(
name|String
name|field
parameter_list|)
block|{
return|return
operator|(
name|ScriptDocValues
operator|.
name|Longs
operator|)
name|doc
argument_list|()
operator|.
name|get
argument_list|(
name|field
argument_list|)
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
comment|/**      * Allows to access statistics on terms and fields.      */
DECL|method|indexLookup
specifier|protected
specifier|final
name|IndexLookup
name|indexLookup
parameter_list|()
block|{
return|return
name|lookup
operator|.
name|indexLookup
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
name|this
operator|.
name|scorer
operator|=
name|scorer
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setNextReader
specifier|public
name|void
name|setNextReader
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
block|{
name|lookup
operator|.
name|setNextReader
argument_list|(
name|context
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

