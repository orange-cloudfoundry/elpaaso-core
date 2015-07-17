/**
 * Copyright (C) 2015 Orange
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.francetelecom.clara.cloud.techmodel.dbaas;

import javax.persistence.Embeddable;

import com.francetelecom.clara.cloud.commons.TechnicalException;

@Embeddable
public class StorageCapacity {

	public enum SizeUnit {

		MB {

			public float toMB(float size) {
				return size;
			};

			public float toGB(float size) {
				return size / 1024;
			};

			public float convert(float size, SizeUnit u) {
				return u.toMB(size);
			}
		},

		GB {
			public float toMB(float size) {
				return size * 1024;
			};

			public float toGB(float size) {
				return size;
			};

			public float convert(float size, SizeUnit u) {
				return u.toGB(size);
			}
		};

		public float convert(float size, SizeUnit u) {
			throw new AbstractMethodError();
		}

		public float toMB(float size) {
			throw new AbstractMethodError();
		}

		public float toGB(float size) {
			throw new AbstractMethodError();
		}
	}

	private float valueInMb;

	protected StorageCapacity() {
	}

	public StorageCapacity(float size, SizeUnit sizeunit) {
		if (size < 1)
			throw new TechnicalException("cannot change database storage capacity. database storage capacity value <" + size + "> should be greater that 1 MB.");
		this.valueInMb = sizeunit.toMB(size);
	}

	public float getValue(SizeUnit sizeunit) {
		return sizeunit.convert(valueInMb, SizeUnit.MB);
	}

	/**
	 * Returns the smallest int value that is greater than or equal to storage
	 * size in required {@code SizeUnit}
	 *
	 *
	 * @param sizeunit
	 *            required {@code SizeUnit}.
	 * @return the smallest int value that is greater than or equal to storage
	 *         size in required {@code SizeUnit}
	 * 
	 */
	public int ceil(SizeUnit sizeunit) {
		return (int) Math.ceil(sizeunit.convert(valueInMb, SizeUnit.MB));
	}


}
