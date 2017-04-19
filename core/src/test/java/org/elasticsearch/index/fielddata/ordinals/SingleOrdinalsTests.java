begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.fielddata.ordinals
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|ordinals
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
name|DocValues
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
name|index
operator|.
name|SortedDocValues
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
name|index
operator|.
name|SortedSetDocValues
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESTestCase
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
name|HashMap
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
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|instanceOf
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|not
import|;
end_import

begin_class
DECL|class|SingleOrdinalsTests
specifier|public
class|class
name|SingleOrdinalsTests
extends|extends
name|ESTestCase
block|{
DECL|method|testSvValues
specifier|public
name|void
name|testSvValues
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|numDocs
init|=
literal|1000000
decl_stmt|;
name|int
name|numOrdinals
init|=
name|numDocs
operator|/
literal|4
decl_stmt|;
name|Map
argument_list|<
name|Integer
argument_list|,
name|Long
argument_list|>
name|controlDocToOrdinal
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|OrdinalsBuilder
name|builder
init|=
operator|new
name|OrdinalsBuilder
argument_list|(
name|numDocs
argument_list|)
decl_stmt|;
name|long
name|ordinal
init|=
name|builder
operator|.
name|currentOrdinal
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|doc
init|=
literal|0
init|;
name|doc
operator|<
name|numDocs
condition|;
name|doc
operator|++
control|)
block|{
if|if
condition|(
name|doc
operator|%
name|numOrdinals
operator|==
literal|0
condition|)
block|{
name|ordinal
operator|=
name|builder
operator|.
name|nextOrdinal
argument_list|()
expr_stmt|;
block|}
name|controlDocToOrdinal
operator|.
name|put
argument_list|(
name|doc
argument_list|,
name|ordinal
argument_list|)
expr_stmt|;
name|builder
operator|.
name|addDoc
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
name|Ordinals
name|ords
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|ords
argument_list|,
name|instanceOf
argument_list|(
name|SinglePackedOrdinals
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|SortedSetDocValues
name|docs
init|=
name|ords
operator|.
name|ordinals
argument_list|()
decl_stmt|;
specifier|final
name|SortedDocValues
name|singleOrds
init|=
name|DocValues
operator|.
name|unwrapSingleton
argument_list|(
name|docs
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|singleOrds
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Integer
argument_list|,
name|Long
argument_list|>
name|entry
range|:
name|controlDocToOrdinal
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|assertTrue
argument_list|(
name|singleOrds
operator|.
name|advanceExact
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|singleOrds
operator|.
name|ordValue
argument_list|()
argument_list|,
operator|(
name|long
operator|)
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testMvOrdinalsTrigger
specifier|public
name|void
name|testMvOrdinalsTrigger
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|numDocs
init|=
literal|1000000
decl_stmt|;
name|OrdinalsBuilder
name|builder
init|=
operator|new
name|OrdinalsBuilder
argument_list|(
name|numDocs
argument_list|)
decl_stmt|;
name|builder
operator|.
name|nextOrdinal
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|doc
init|=
literal|0
init|;
name|doc
operator|<
name|numDocs
condition|;
name|doc
operator|++
control|)
block|{
name|builder
operator|.
name|addDoc
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
name|Ordinals
name|ords
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|ords
argument_list|,
name|instanceOf
argument_list|(
name|SinglePackedOrdinals
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|nextOrdinal
argument_list|()
expr_stmt|;
name|builder
operator|.
name|addDoc
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|ords
operator|=
name|builder
operator|.
name|build
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|ords
argument_list|,
name|not
argument_list|(
name|instanceOf
argument_list|(
name|SinglePackedOrdinals
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

