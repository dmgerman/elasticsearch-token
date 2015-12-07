begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
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
name|XContentHelper
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
name|ArrayList
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
name|Map
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
name|xcontent
operator|.
name|ToXContent
operator|.
name|EMPTY_PARAMS
import|;
end_import

begin_class
DECL|class|XContentTestUtils
specifier|public
specifier|final
class|class
name|XContentTestUtils
block|{
DECL|method|XContentTestUtils
specifier|private
name|XContentTestUtils
parameter_list|()
block|{      }
DECL|method|convertToMap
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|convertToMap
parameter_list|(
name|ToXContent
name|part
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|part
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|XContentHelper
operator|.
name|convertToMap
argument_list|(
name|builder
operator|.
name|bytes
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|v2
argument_list|()
return|;
block|}
comment|/**      * Compares to maps generated from XContentObjects. The order of elements in arrays is ignored.      *      * @return null if maps are equal or path to the element where the difference was found      */
DECL|method|differenceBetweenMapsIgnoringArrayOrder
specifier|public
specifier|static
name|String
name|differenceBetweenMapsIgnoringArrayOrder
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|first
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|second
parameter_list|)
block|{
return|return
name|differenceBetweenMapsIgnoringArrayOrder
argument_list|(
literal|""
argument_list|,
name|first
argument_list|,
name|second
argument_list|)
return|;
block|}
DECL|method|differenceBetweenMapsIgnoringArrayOrder
specifier|private
specifier|static
name|String
name|differenceBetweenMapsIgnoringArrayOrder
parameter_list|(
name|String
name|path
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|first
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|second
parameter_list|)
block|{
if|if
condition|(
name|first
operator|.
name|size
argument_list|()
operator|!=
name|second
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
name|path
operator|+
literal|": sizes of the maps don't match: "
operator|+
name|first
operator|.
name|size
argument_list|()
operator|+
literal|" != "
operator|+
name|second
operator|.
name|size
argument_list|()
return|;
block|}
for|for
control|(
name|String
name|key
range|:
name|first
operator|.
name|keySet
argument_list|()
control|)
block|{
name|String
name|reason
init|=
name|differenceBetweenObjectsIgnoringArrayOrder
argument_list|(
name|path
operator|+
literal|"/"
operator|+
name|key
argument_list|,
name|first
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|,
name|second
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|reason
operator|!=
literal|null
condition|)
block|{
return|return
name|reason
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|differenceBetweenObjectsIgnoringArrayOrder
specifier|private
specifier|static
name|String
name|differenceBetweenObjectsIgnoringArrayOrder
parameter_list|(
name|String
name|path
parameter_list|,
name|Object
name|first
parameter_list|,
name|Object
name|second
parameter_list|)
block|{
if|if
condition|(
name|first
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|second
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
return|return
name|path
operator|+
literal|": first element is null, the second element is not null"
return|;
block|}
block|}
elseif|else
if|if
condition|(
name|first
operator|instanceof
name|List
condition|)
block|{
if|if
condition|(
name|second
operator|instanceof
name|List
condition|)
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|secondList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|second
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|firstList
init|=
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|first
decl_stmt|;
if|if
condition|(
name|firstList
operator|.
name|size
argument_list|()
operator|==
name|secondList
operator|.
name|size
argument_list|()
condition|)
block|{
name|String
name|reason
init|=
name|path
operator|+
literal|": no matches found"
decl_stmt|;
for|for
control|(
name|Object
name|firstObj
range|:
name|firstList
control|)
block|{
name|boolean
name|found
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Object
name|secondObj
range|:
name|secondList
control|)
block|{
name|reason
operator|=
name|differenceBetweenObjectsIgnoringArrayOrder
argument_list|(
name|path
operator|+
literal|"/*"
argument_list|,
name|firstObj
argument_list|,
name|secondObj
argument_list|)
expr_stmt|;
if|if
condition|(
name|reason
operator|==
literal|null
condition|)
block|{
name|secondList
operator|.
name|remove
argument_list|(
name|secondObj
argument_list|)
expr_stmt|;
name|found
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|found
operator|==
literal|false
condition|)
block|{
return|return
name|reason
return|;
block|}
block|}
if|if
condition|(
name|secondList
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
return|return
name|path
operator|+
literal|": the second list is not empty"
return|;
block|}
block|}
else|else
block|{
return|return
name|path
operator|+
literal|": sizes of the arrays don't match: "
operator|+
name|firstList
operator|.
name|size
argument_list|()
operator|+
literal|" != "
operator|+
name|secondList
operator|.
name|size
argument_list|()
return|;
block|}
block|}
else|else
block|{
return|return
name|path
operator|+
literal|": the second element is not an array"
return|;
block|}
block|}
elseif|else
if|if
condition|(
name|first
operator|instanceof
name|Map
condition|)
block|{
if|if
condition|(
name|second
operator|instanceof
name|Map
condition|)
block|{
return|return
name|differenceBetweenMapsIgnoringArrayOrder
argument_list|(
name|path
argument_list|,
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|first
argument_list|,
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|second
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|path
operator|+
literal|": the second element is not a map (got "
operator|+
name|second
operator|+
literal|")"
return|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|first
operator|.
name|equals
argument_list|(
name|second
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
return|return
name|path
operator|+
literal|": the elements don't match: ["
operator|+
name|first
operator|+
literal|"] != ["
operator|+
name|second
operator|+
literal|"]"
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

