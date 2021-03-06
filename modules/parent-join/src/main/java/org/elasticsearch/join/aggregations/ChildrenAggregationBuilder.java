begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.join.aggregations
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|join
operator|.
name|aggregations
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
name|search
operator|.
name|Query
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
name|ParsingException
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
name|fielddata
operator|.
name|plain
operator|.
name|SortedSetDVOrdinalsIndexFieldData
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
name|mapper
operator|.
name|DocumentMapper
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
name|mapper
operator|.
name|MappedFieldType
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
name|mapper
operator|.
name|ParentFieldMapper
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
name|QueryParseContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|join
operator|.
name|mapper
operator|.
name|ParentIdFieldMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|join
operator|.
name|mapper
operator|.
name|ParentJoinFieldMapper
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
name|aggregations
operator|.
name|AggregatorFactories
operator|.
name|Builder
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
name|aggregations
operator|.
name|AggregatorFactory
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
name|aggregations
operator|.
name|support
operator|.
name|FieldContext
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
name|aggregations
operator|.
name|support
operator|.
name|ValueType
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
name|aggregations
operator|.
name|support
operator|.
name|ValuesSource
operator|.
name|Bytes
operator|.
name|WithOrdinals
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
name|aggregations
operator|.
name|support
operator|.
name|ValuesSourceAggregationBuilder
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
name|aggregations
operator|.
name|support
operator|.
name|ValuesSourceAggregatorFactory
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
name|aggregations
operator|.
name|support
operator|.
name|ValuesSourceConfig
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
name|aggregations
operator|.
name|support
operator|.
name|ValuesSourceType
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
name|Objects
import|;
end_import

begin_class
DECL|class|ChildrenAggregationBuilder
specifier|public
class|class
name|ChildrenAggregationBuilder
extends|extends
name|ValuesSourceAggregationBuilder
argument_list|<
name|WithOrdinals
argument_list|,
name|ChildrenAggregationBuilder
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"children"
decl_stmt|;
DECL|field|childType
specifier|private
specifier|final
name|String
name|childType
decl_stmt|;
DECL|field|parentFilter
specifier|private
name|Query
name|parentFilter
decl_stmt|;
DECL|field|childFilter
specifier|private
name|Query
name|childFilter
decl_stmt|;
comment|/**      * @param name      *            the name of this aggregation      * @param childType      *            the type of children documents      */
DECL|method|ChildrenAggregationBuilder
specifier|public
name|ChildrenAggregationBuilder
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|childType
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|ValuesSourceType
operator|.
name|BYTES
argument_list|,
name|ValueType
operator|.
name|STRING
argument_list|)
expr_stmt|;
if|if
condition|(
name|childType
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[childType] must not be null: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|childType
operator|=
name|childType
expr_stmt|;
block|}
comment|/**      * Read from a stream.      */
DECL|method|ChildrenAggregationBuilder
specifier|public
name|ChildrenAggregationBuilder
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|,
name|ValuesSourceType
operator|.
name|BYTES
argument_list|,
name|ValueType
operator|.
name|STRING
argument_list|)
expr_stmt|;
name|childType
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|innerWriteTo
specifier|protected
name|void
name|innerWriteTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeString
argument_list|(
name|childType
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|innerBuild
specifier|protected
name|ValuesSourceAggregatorFactory
argument_list|<
name|WithOrdinals
argument_list|,
name|?
argument_list|>
name|innerBuild
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|ValuesSourceConfig
argument_list|<
name|WithOrdinals
argument_list|>
name|config
parameter_list|,
name|AggregatorFactory
argument_list|<
name|?
argument_list|>
name|parent
parameter_list|,
name|Builder
name|subFactoriesBuilder
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|ChildrenAggregatorFactory
argument_list|(
name|name
argument_list|,
name|config
argument_list|,
name|childFilter
argument_list|,
name|parentFilter
argument_list|,
name|context
argument_list|,
name|parent
argument_list|,
name|subFactoriesBuilder
argument_list|,
name|metaData
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|resolveConfig
specifier|protected
name|ValuesSourceConfig
argument_list|<
name|WithOrdinals
argument_list|>
name|resolveConfig
parameter_list|(
name|SearchContext
name|context
parameter_list|)
block|{
name|ValuesSourceConfig
argument_list|<
name|WithOrdinals
argument_list|>
name|config
init|=
operator|new
name|ValuesSourceConfig
argument_list|<>
argument_list|(
name|ValuesSourceType
operator|.
name|BYTES
argument_list|)
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|mapperService
argument_list|()
operator|.
name|getIndexSettings
argument_list|()
operator|.
name|isSingleType
argument_list|()
condition|)
block|{
name|joinFieldResolveConfig
argument_list|(
name|context
argument_list|,
name|config
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|parentFieldResolveConfig
argument_list|(
name|context
argument_list|,
name|config
argument_list|)
expr_stmt|;
block|}
return|return
name|config
return|;
block|}
DECL|method|joinFieldResolveConfig
specifier|private
name|void
name|joinFieldResolveConfig
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|ValuesSourceConfig
argument_list|<
name|WithOrdinals
argument_list|>
name|config
parameter_list|)
block|{
name|ParentJoinFieldMapper
name|parentJoinFieldMapper
init|=
name|ParentJoinFieldMapper
operator|.
name|getMapper
argument_list|(
name|context
operator|.
name|mapperService
argument_list|()
argument_list|)
decl_stmt|;
name|ParentIdFieldMapper
name|parentIdFieldMapper
init|=
name|parentJoinFieldMapper
operator|.
name|getParentIdFieldMapper
argument_list|(
name|childType
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|parentIdFieldMapper
operator|!=
literal|null
condition|)
block|{
name|parentFilter
operator|=
name|parentIdFieldMapper
operator|.
name|getParentFilter
argument_list|()
expr_stmt|;
name|childFilter
operator|=
name|parentIdFieldMapper
operator|.
name|getChildFilter
argument_list|(
name|childType
argument_list|)
expr_stmt|;
name|MappedFieldType
name|fieldType
init|=
name|parentIdFieldMapper
operator|.
name|fieldType
argument_list|()
decl_stmt|;
specifier|final
name|SortedSetDVOrdinalsIndexFieldData
name|fieldData
init|=
name|context
operator|.
name|fieldData
argument_list|()
operator|.
name|getForField
argument_list|(
name|fieldType
argument_list|)
decl_stmt|;
name|config
operator|.
name|fieldContext
argument_list|(
operator|new
name|FieldContext
argument_list|(
name|fieldType
operator|.
name|name
argument_list|()
argument_list|,
name|fieldData
argument_list|,
name|fieldType
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|config
operator|.
name|unmapped
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|parentFieldResolveConfig
specifier|private
name|void
name|parentFieldResolveConfig
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|ValuesSourceConfig
argument_list|<
name|WithOrdinals
argument_list|>
name|config
parameter_list|)
block|{
name|DocumentMapper
name|childDocMapper
init|=
name|context
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapper
argument_list|(
name|childType
argument_list|)
decl_stmt|;
if|if
condition|(
name|childDocMapper
operator|!=
literal|null
condition|)
block|{
name|ParentFieldMapper
name|parentFieldMapper
init|=
name|childDocMapper
operator|.
name|parentFieldMapper
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|parentFieldMapper
operator|.
name|active
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[children] no [_parent] field not configured that points to a parent type"
argument_list|)
throw|;
block|}
name|String
name|parentType
init|=
name|parentFieldMapper
operator|.
name|type
argument_list|()
decl_stmt|;
name|DocumentMapper
name|parentDocMapper
init|=
name|context
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapper
argument_list|(
name|parentType
argument_list|)
decl_stmt|;
if|if
condition|(
name|parentDocMapper
operator|!=
literal|null
condition|)
block|{
name|parentFilter
operator|=
name|parentDocMapper
operator|.
name|typeFilter
argument_list|(
name|context
operator|.
name|getQueryShardContext
argument_list|()
argument_list|)
expr_stmt|;
name|childFilter
operator|=
name|childDocMapper
operator|.
name|typeFilter
argument_list|(
name|context
operator|.
name|getQueryShardContext
argument_list|()
argument_list|)
expr_stmt|;
name|MappedFieldType
name|parentFieldType
init|=
name|parentDocMapper
operator|.
name|parentFieldMapper
argument_list|()
operator|.
name|getParentJoinFieldType
argument_list|()
decl_stmt|;
specifier|final
name|SortedSetDVOrdinalsIndexFieldData
name|fieldData
init|=
name|context
operator|.
name|fieldData
argument_list|()
operator|.
name|getForField
argument_list|(
name|parentFieldType
argument_list|)
decl_stmt|;
name|config
operator|.
name|fieldContext
argument_list|(
operator|new
name|FieldContext
argument_list|(
name|parentFieldType
operator|.
name|name
argument_list|()
argument_list|,
name|fieldData
argument_list|,
name|parentFieldType
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|config
operator|.
name|unmapped
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|config
operator|.
name|unmapped
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|doXContentBody
specifier|protected
name|XContentBuilder
name|doXContentBody
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
name|field
argument_list|(
name|ParentToChildrenAggregator
operator|.
name|TYPE_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|childType
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|parse
specifier|public
specifier|static
name|ChildrenAggregationBuilder
name|parse
parameter_list|(
name|String
name|aggregationName
parameter_list|,
name|QueryParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|childType
init|=
literal|null
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|XContentParser
name|parser
init|=
name|context
operator|.
name|parser
argument_list|()
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_STRING
condition|)
block|{
if|if
condition|(
literal|"type"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|childType
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Unknown key for a "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|aggregationName
operator|+
literal|"]: ["
operator|+
name|currentFieldName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Unexpected token "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|aggregationName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|childType
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Missing [child_type] field for children aggregation ["
operator|+
name|aggregationName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
operator|new
name|ChildrenAggregationBuilder
argument_list|(
name|aggregationName
argument_list|,
name|childType
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|innerHashCode
specifier|protected
name|int
name|innerHashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|childType
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|innerEquals
specifier|protected
name|boolean
name|innerEquals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
name|ChildrenAggregationBuilder
name|other
init|=
operator|(
name|ChildrenAggregationBuilder
operator|)
name|obj
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|childType
argument_list|,
name|other
operator|.
name|childType
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getType
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
block|}
end_class

end_unit

