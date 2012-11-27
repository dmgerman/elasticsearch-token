begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.aliases
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|aliases
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|UnmodifiableIterator
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
name|queries
operator|.
name|FilterClause
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
name|BooleanClause
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
name|Filter
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
name|Nullable
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
name|compress
operator|.
name|CompressedString
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
name|lucene
operator|.
name|search
operator|.
name|XBooleanFilter
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
name|xcontent
operator|.
name|XContentFactory
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
name|XContentParser
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
name|AbstractIndexComponent
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
name|Index
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
name|query
operator|.
name|IndexQueryParserService
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
name|settings
operator|.
name|IndexSettings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|AliasFilterParsingException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|InvalidAliasNameException
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|collect
operator|.
name|MapBuilder
operator|.
name|newMapBuilder
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|IndexAliasesService
specifier|public
class|class
name|IndexAliasesService
extends|extends
name|AbstractIndexComponent
implements|implements
name|Iterable
argument_list|<
name|IndexAlias
argument_list|>
block|{
DECL|field|indexQueryParser
specifier|private
specifier|final
name|IndexQueryParserService
name|indexQueryParser
decl_stmt|;
DECL|field|aliases
specifier|private
specifier|volatile
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|IndexAlias
argument_list|>
name|aliases
init|=
name|ImmutableMap
operator|.
name|of
argument_list|()
decl_stmt|;
DECL|field|mutex
specifier|private
specifier|final
name|Object
name|mutex
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
annotation|@
name|Inject
DECL|method|IndexAliasesService
specifier|public
name|IndexAliasesService
parameter_list|(
name|Index
name|index
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|IndexQueryParserService
name|indexQueryParser
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
name|this
operator|.
name|indexQueryParser
operator|=
name|indexQueryParser
expr_stmt|;
block|}
DECL|method|hasAlias
specifier|public
name|boolean
name|hasAlias
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
return|return
name|aliases
operator|.
name|containsKey
argument_list|(
name|alias
argument_list|)
return|;
block|}
DECL|method|alias
specifier|public
name|IndexAlias
name|alias
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
return|return
name|aliases
operator|.
name|get
argument_list|(
name|alias
argument_list|)
return|;
block|}
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|String
name|alias
parameter_list|,
annotation|@
name|Nullable
name|CompressedString
name|filter
parameter_list|)
block|{
name|add
argument_list|(
operator|new
name|IndexAlias
argument_list|(
name|alias
argument_list|,
name|filter
argument_list|,
name|parse
argument_list|(
name|alias
argument_list|,
name|filter
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns the filter associated with listed filtering aliases.      *<p/>      *<p>The list of filtering aliases should be obtained by calling MetaData.filteringAliases.      * Returns<tt>null</tt> if no filtering is required.</p>      */
DECL|method|aliasFilter
specifier|public
name|Filter
name|aliasFilter
parameter_list|(
name|String
modifier|...
name|aliases
parameter_list|)
block|{
if|if
condition|(
name|aliases
operator|==
literal|null
operator|||
name|aliases
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|aliases
operator|.
name|length
operator|==
literal|1
condition|)
block|{
name|IndexAlias
name|indexAlias
init|=
name|alias
argument_list|(
name|aliases
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexAlias
operator|==
literal|null
condition|)
block|{
comment|// This shouldn't happen unless alias disappeared after filteringAliases was called.
throw|throw
operator|new
name|InvalidAliasNameException
argument_list|(
name|index
argument_list|,
name|aliases
index|[
literal|0
index|]
argument_list|,
literal|"Unknown alias name was passed to alias Filter"
argument_list|)
throw|;
block|}
return|return
name|indexAlias
operator|.
name|parsedFilter
argument_list|()
return|;
block|}
else|else
block|{
comment|// we need to bench here a bit, to see maybe it makes sense to use OrFilter
name|XBooleanFilter
name|combined
init|=
operator|new
name|XBooleanFilter
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|alias
range|:
name|aliases
control|)
block|{
name|IndexAlias
name|indexAlias
init|=
name|alias
argument_list|(
name|alias
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexAlias
operator|==
literal|null
condition|)
block|{
comment|// This shouldn't happen unless alias disappeared after filteringAliases was called.
throw|throw
operator|new
name|InvalidAliasNameException
argument_list|(
name|index
argument_list|,
name|aliases
index|[
literal|0
index|]
argument_list|,
literal|"Unknown alias name was passed to alias Filter"
argument_list|)
throw|;
block|}
if|if
condition|(
name|indexAlias
operator|.
name|parsedFilter
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|combined
operator|.
name|add
argument_list|(
operator|new
name|FilterClause
argument_list|(
name|indexAlias
operator|.
name|parsedFilter
argument_list|()
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// The filter might be null only if filter was removed after filteringAliases was called
return|return
literal|null
return|;
block|}
block|}
if|if
condition|(
name|combined
operator|.
name|clauses
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|combined
operator|.
name|clauses
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
return|return
name|combined
operator|.
name|clauses
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getFilter
argument_list|()
return|;
block|}
return|return
name|combined
return|;
block|}
block|}
DECL|method|add
specifier|private
name|void
name|add
parameter_list|(
name|IndexAlias
name|indexAlias
parameter_list|)
block|{
synchronized|synchronized
init|(
name|mutex
init|)
block|{
name|aliases
operator|=
name|newMapBuilder
argument_list|(
name|aliases
argument_list|)
operator|.
name|put
argument_list|(
name|indexAlias
operator|.
name|alias
argument_list|()
argument_list|,
name|indexAlias
argument_list|)
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|remove
specifier|public
name|void
name|remove
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
synchronized|synchronized
init|(
name|mutex
init|)
block|{
name|aliases
operator|=
name|newMapBuilder
argument_list|(
name|aliases
argument_list|)
operator|.
name|remove
argument_list|(
name|alias
argument_list|)
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|parse
specifier|private
name|Filter
name|parse
parameter_list|(
name|String
name|alias
parameter_list|,
name|CompressedString
name|filter
parameter_list|)
block|{
if|if
condition|(
name|filter
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
try|try
block|{
name|byte
index|[]
name|filterSource
init|=
name|filter
operator|.
name|uncompressed
argument_list|()
decl_stmt|;
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|filterSource
argument_list|)
operator|.
name|createParser
argument_list|(
name|filterSource
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|indexQueryParser
operator|.
name|parseInnerFilter
argument_list|(
name|parser
argument_list|)
return|;
block|}
finally|finally
block|{
name|parser
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|AliasFilterParsingException
argument_list|(
name|index
argument_list|,
name|alias
argument_list|,
literal|"Invalid alias filter"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|UnmodifiableIterator
argument_list|<
name|IndexAlias
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|aliases
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
block|}
end_class

end_unit

